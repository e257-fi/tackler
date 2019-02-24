/*
 * Copyright 2016-2019 E257.FI
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

import io.circe._
import io.circe.syntax._

import fi.e257.tackler.api.{BalanceItem, BalanceReport, Delta, OrderByDelta}
import fi.e257.tackler.core._
import fi.e257.tackler.model.{BalanceTreeNode, TxnData}

abstract class BalanceReporterLike(cfg: ReportConfiguration) extends ReportLike(cfg) {

  private def getBalanceBodyText(balance: Balance): (Seq[String], Seq[String]) = {

    /*
     * param f, select which balance sum type to use, account or accountTree
     * return max needed width of selected balance sum type
     */
    def getMaxSumLen()(f: (BalanceTreeNode => BigDecimal)): Int = {
      balance.bal.map(b => ("% " + getScaleFormat(f(b))).format(f(b)).length).foldLeft(0)(math.max)
    }

    def getMaxDeltaLen(): Int = {
      balance.deltas.map(d => {
        ("% " + getScaleFormat(d._2)).format(d._2).length
      }).foldLeft(0)(math.max)
    }

    /*
     * All balance account commodities are present on deltas
     * so this is also max length of all commodities
     */
    def getMaxCommodityLen(): Int = {
      balance.deltas.map(d => {
        d._1.map(c => c.name.length).getOrElse(0)
      }).foldLeft(0)(math.max)
    }

    val maxAccSumLen = List(getMaxDeltaLen(), getMaxSumLen()(b => b.accountSum)).foldLeft(12)(math.max)
    val maxSubAccSumLen = getMaxSumLen()(b => b.subAccTreeSum)

    def getAccSumField(b: BalanceTreeNode): String = fillFormat(maxAccSumLen, b.accountSum)
    def getAccTreeSumField(b: BalanceTreeNode): String = fillFormat(maxSubAccSumLen, b.subAccTreeSum)


    val maxCommLen = getMaxCommodityLen()
    val commFrmt = "%-" + "%d".format(maxCommLen) + "s"

    /*
     * filler between account sums (acc and accTree sums)
     * Width of this filler is mandated by delta sum's max commodity length,
     * because then AccTreesSum won't overlap with delta's commodity
     */
    val fillerField = {
      if (maxCommLen > 0)
        " " * (4 + maxCommLen)
      else
        " " * 3
    }

    def getCommodityField(b: BalanceTreeNode): String = {
      if (maxCommLen > 0) {
        b.acctn.commodity match {
          case Some(c) => " " + commFrmt.format(c.name) + "  "
          case None => " " + (" " * maxCommLen) + "  "
        }
      } else {
        // always separate with two spaces
        "  "
      }
    }

    val body = balance.bal.map(b => {
        " " * 9 +
          getAccSumField(b) +
          fillerField +
          getAccTreeSumField(b) +
          getCommodityField(b) + b.acctn.account
      })

    val footer = balance.deltas.toSeq.sortBy({ case (cOpt, _) =>
      cOpt.map(c => c.name).getOrElse("")
    }).map({ case (cOpt, v) =>
      " " * 9 + fillFormat(maxAccSumLen, v) + cOpt.map(c => " " + c.name).getOrElse("")
    })

    (body, footer)
  }

  protected def addFooter(footer: Seq[String]) = {
    List("=" * footer.map(_.length).foldLeft(0)(math.max)) ++ footer
  }

  protected def txtBalanceBody(balance: Balance): (Seq[String], Seq[String]) = {
    if (balance.isEmpty) {
      (Seq.empty[String], Seq.empty[String])
    } else {
      getBalanceBodyText(balance)
    }
  }

  protected def btnToApi(btn: BalanceTreeNode): BalanceItem = {
    BalanceItem(
      accountSum = scaleFormat(btn.accountSum),
      accountTreeSum = scaleFormat(btn.subAccTreeSum),
      account = btn.acctn.account,
      commodity = btn.acctn.commodity.map(_.name)
    )
  }

  protected def balanceToApi(balance:Balance): BalanceReport = {

    val body = balance.bal.map(btnToApi)

    val deltas = balance.deltas.toSeq
      .map({ case (c, v) =>
        Delta(
          commodity = c.map(_.name),
          delta = scaleFormat(v))
      })
      .sorted(OrderByDelta)

    BalanceReport(balance.metadata, balance.title, body, deltas)
  }
}

class BalanceReporter(val mySettings: BalanceSettings) extends  BalanceReporterLike(mySettings) {

  override val name = mySettings.outputname

  protected def txtBalanceReport(bal: Balance): Seq[String] = {

    val (body, footer) = txtBalanceBody(bal)

    val  header = List(
      bal.metadata.fold(""){md => md.text()},
      bal.title,
      "-" * bal.title.length)

    if (body.isEmpty) {
      header
    } else {
      header ++ body ++ addFooter(footer)
    }
  }

  protected def jsonBalanceReport(bal: Balance): Json = {
    balanceToApi(bal).asJson
  }

  protected def getBalance(txns: TxnData): Balance = {
    val bf = if (mySettings.accounts.isEmpty) {
      new AllBalanceAccounts(mySettings.hash)
    } else {
      new BalanceFilterByAccount(mySettings.accounts, mySettings.hash)
    }

    Balance(mySettings.title, txns, bf)
  }

  override
  def jsonReport(txnData: TxnData): Json = {
    val bal = getBalance(txnData)
    jsonBalanceReport(bal)
  }

  override
  def writeReport(formats: Formats, txnData: TxnData): Unit = {

    val bal = getBalance(txnData)

    formats.foreach({case (format, writers) =>
      format match {
        case TextFormat() => {
          doRowOutputs(writers, txtBalanceReport(bal))
        }
        case JsonFormat() => {
          doRowOutputs(writers, Seq(jsonBalanceReport(bal).pretty(printer)))
        }
      }
    })
  }
}
