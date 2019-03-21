/*
 * Copyright 2016-2018 E257.FI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package fi.e257.tackler.tools.generator

import java.time.format.DateTimeFormatter
import java.time.{Duration, ZoneId, ZonedDateTime}
import java.util.UUID

import better.files._

import scala.util.control.NonFatal

object Generator {
  val SUCCESS: Int = 0
  val FAILURE: Int = 127

  def nameUUID(name: String): String = {
    // no real RFC-4122 namespace, this is ok for this purpose
    UUID.nameUUIDFromBytes(name.getBytes("UTF-8")).toString
  }

  def run(args: Array[String]): Unit = {

    val cliCfg = new GeneratorCLIArgs(args)

    val countStr = cliCfg.count.getOrElse("none")
    val count =   countStr match {
      case "1E1" => 10
      case "1E2" => 100
      case "1E3" => 1000
      case "1E4" => 10000
      case "1E5" => 100000
      case "1E6" => 1000000
      case _ => throw new RuntimeException("Unknown count, should be [1E1, 1E2, 1E3, 1E4, 1E5, 1E6] it was: " + countStr)
    }


    val basedir = cliCfg.basedir.getOrElse("./data")
    val txnsDir = File(basedir, s"txns-$countStr" )

    val startTS = ZonedDateTime.of(2016, 1, 1, 0, 0, 0, 0, ZoneId.of("Z"))
    val endTS = ZonedDateTime.of(2016, 12, 31, 23, 59, 59, 0, ZoneId.of("Z"))
    val duration = Duration.between(startTS, endTS)
    val step = duration.getSeconds / count

    if (cliCfg.single_file.getOrElse(false)) {
      File(basedir).createDirectories()
      val txnFile = File(basedir, "txns-" + countStr + ".txn")
      txnFile.createIfNotExists().overwrite("")
    }

    val accounts: Seq[List[String]] = for (i <- 1 to count) yield {
      val ts = startTS.plusSeconds(i * step)
      val y = ts.getYear
      val m = ts.getMonthValue
      val d = ts.getDayOfMonth

      val assetsAcc = "a:ay%04d:am%02d".format(y, m)
      val expensesAcc = "e:ey%04d:em%02d:ed%02d".format(y, m, d)

      val compatStr = if (cliCfg.compatible.getOrElse(false)) {
        (ts.format(DateTimeFormatter.ofPattern("yyyy'/'MM'/'dd")), "  ")
      } else {
        (ts.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), " ")
      }

      compatStr match {
        case (tsStr, valSpace) =>

          val code = s"(#%07d)".format(i)
          val txn = tsStr + " " + code + " '" + countStr + s" txn-%d".format(i) + "\n" +
            (if (cliCfg.compatible.getOrElse(false)) {
              ""
            } else {
              // Generate UUID so that each set has own predictable set of UUIDs.
              // e.g. uuid differs between sets (1E2 vs. 1E3) for txn-1, txn-2 etc.
              " # uuid: " + nameUUID(countStr + code) + "\n"
            }) +
            s""" $expensesAcc$valSpace$d.0000001
               | $assetsAcc
               |
               |""".stripMargin

          if (cliCfg.single_file.getOrElse(false)) {
            val txnFile = File(basedir, "perf-" + countStr + ".txn")
            txnFile.append(txn)
          } else {
            val txnName = ts.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")) + s"-$i.txn"
            val txnShardDir = txnsDir / "%04d/%02d/%02d".format(y, m, d)
            val txnFile = txnShardDir / txnName

            txnShardDir.createDirectories()
            txnFile.createIfNotExists().overwrite(txn)
          }

          List(expensesAcc, assetsAcc)
      }
    }

    val coaConf = accounts.flatten.sorted.distinct.mkString(
        "accounts {\n\n  permit-empty-commodity = true\n\n  coa = [\n    \"",
        "\",\n    \"",
        "\"\n  ]\n}\n")

    val coaFile = File(basedir, s"txns-$countStr-accounts.conf")
    coaFile.overwrite(coaConf)
  }

  def runReturnValue(args: Array[String]): Int = {
    try {
      run(args)
      SUCCESS
    } catch {
      case org.rogach.scallop.exceptions.Help("") =>
        // do not report success
        FAILURE

      case ex: org.rogach.scallop.exceptions.ScallopException =>
        // Already printed by scallop
        FAILURE

      case NonFatal(ex) =>
        Console.err.println(ex.getMessage)
        FAILURE
    }
  }

  def main(args: Array[String]): Unit = {

    System.exit(runReturnValue(args))
  }
}
