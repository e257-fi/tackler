/*
 * Copyright 2016-2020 E257.FI
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
package fi.e257.tackler.report

import io.circe.Json
import io.circe.syntax._
import fi.e257.tackler.api.{BalanceGroupReport, Metadata, TimeZoneInfo, TxnTS}
import fi.e257.tackler.core._
import fi.e257.tackler.model.{Transaction, TxnData}
import fi.e257.tackler.Scala12to13.Converters._


class BalanceGroupReporter(val mySettings: BalanceGroupSettings) extends BalanceReporterLike(mySettings) {

  override val name: String = mySettings.outputname

  protected def txtBalanceGroupReport(metadata: Option[Metadata], balGrps: Seq[Balance]): Seq[String] = {

    val header = List(
      metadata.fold(""){md => md.text()},
      mySettings.title,
      "-" * mySettings.title.length)

    val body = balGrps.par.flatMap(bal => txtBalanceGroup(bal))

    header ++ body
  }

  protected def txtBalanceGroup(bal: Balance): Seq[String] = {

    val (body, footer) = txtBalanceBody(bal)
    val title = bal.title
    val  header = List(
      title,
      "-" * title.length)

    header ++ body ++ addFooter(footer)
  }

  protected def getBalanceGroups(txnData: TxnData): (Option[Metadata], Seq[Balance]) = {

    val balanceFilter = if (mySettings.accounts.isEmpty) {
      new AllBalanceAccounts(mySettings.hash)
    } else {
      new BalanceFilterByAccount(mySettings.accounts, mySettings.hash)
    }

    val groupOp = mySettings.reportTZ match {
      case Some(reportTZ) => {
        mySettings.groupBy match {
          case GroupByYear() => { txn: Transaction =>
            TxnTS.localYear(txn.header.timestamp, reportTZ)
          }
          case GroupByMonth() => { txn: Transaction =>
            TxnTS.localMonth(txn.header.timestamp, reportTZ)
          }
          case GroupByDate() => { txn: Transaction =>
            TxnTS.localDate(txn.header.timestamp, reportTZ)
          }
          case GroupByIsoWeek() => { txn: Transaction =>
            TxnTS.localWeek(txn.header.timestamp, reportTZ)
          }
          case GroupByIsoWeekDate() => { txn: Transaction =>
            TxnTS.localWeekDate(txn.header.timestamp, reportTZ)
          }
        }
      }
      case None => {
        mySettings.groupBy match {
          case GroupByYear() => { txn: Transaction =>
            TxnTS.tzYear(txn.header.timestamp)
          }
          case GroupByMonth() => { txn: Transaction =>
            TxnTS.tzMonth(txn.header.timestamp)
          }
          case GroupByDate() => { txn: Transaction =>
            TxnTS.tzDate(txn.header.timestamp)
          }
          case GroupByIsoWeek() => { txn: Transaction =>
            TxnTS.tzWeek(txn.header.timestamp)
          }
          case GroupByIsoWeekDate() => { txn: Transaction =>
            TxnTS.tzWeekDate(txn.header.timestamp)
          }
        }
      }
    }

    val md = Metadata.append(txnData.getMetadata(balanceFilter), TimeZoneInfo(mySettings.reportTZ))

    (md, Accumulator.balanceGroups(txnData, groupOp, balanceFilter))
  }

  protected def jsonBalanceGroupReport(metadata: Option[Metadata], balGrps: Seq[Balance]): Json = {

    val bgs = balGrps.par.map(balanceToApi).seq

    BalanceGroupReport(metadata, mySettings.title, bgs).asJson
  }

  override
  def jsonReport(txnData: TxnData): Json = {
    val balGrps = getBalanceGroups(txnData)
    jsonBalanceGroupReport(balGrps._1, balGrps._2)
  }

  override
  def writeReport(formats: Formats, txnData: TxnData): Unit = {

    val balGrps = getBalanceGroups(txnData)

    formats.foreach({case (format, writers) =>
      format match {
        case TextFormat() =>
          doRowOutputs(writers, txtBalanceGroupReport(balGrps._1, balGrps._2))

        case JsonFormat() => {
          doRowOutputs(writers, Seq(jsonBalanceGroupReport(balGrps._1, balGrps._2).printWith(printer)))
        }
      }
    })
  }
}
