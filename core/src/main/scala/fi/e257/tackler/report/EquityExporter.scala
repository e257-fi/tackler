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

import fi.e257.tackler.api.TxnTS
import fi.e257.tackler.core._
import fi.e257.tackler.math._
import fi.e257.tackler.model.TxnData

class EquityExporter(val settings: Settings) extends ExporterLike {
  private val mySettings = settings.Exports.Equity

  @SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
  def txnEquity(txnData: TxnData): Seq[String] = {

    val bf = if (mySettings.accounts.isEmpty) {
      new BalanceFilterNonZero(settings.Auditing.hash)
    } else {
      new BalanceFilterNonZeroByAccount(mySettings.accounts, settings.Auditing.hash)
    }
    val bal = Balance("", txnData, bf)

    if (bal.isEmpty) {
      Nil
    } else {
      val eqTxnIndent = "   "
      val lastTxn = txnData.txns.last
      def eqTxnHeader(commStr: String) = {
        val c = if (commStr.isEmpty) {
          ""
        } else {
          " for " + commStr
        }
        TxnTS.isoZonedTS(lastTxn.header.timestamp) + " " + "'Equity" + c + lastTxn.header.uuid.map(u => ": last txn (uuid): " + u.toString).getOrElse("")
      }

      bal.bal
        .groupBy(b => b.acctn.commStr)
        .toSeq.sortBy({ case (commStr, _) => commStr }) // Scala 2.12 vs. 2.13: fix order sorting by commodity
        .flatMap({ case (commStr, bs) =>
        val delta = bs.map(b => b.accountSum).realSum
        val eqBalRow = if (delta.isZero) {
          Nil
        } else {
          val deltaStr = (-delta).toString() + (if (commStr.isEmpty) {
            ""
          } else {
            " " + commStr
          })
          List(eqTxnIndent + "Equity:Balance" + "  " + deltaStr)
        }

        List(eqTxnHeader(commStr)) ++
          bal.metadata.map(md => md.mkString(eqTxnIndent + "; ", "\n" + eqTxnIndent +"; ", "")).toList ++
            (if (eqBalRow.isEmpty) {
              List(
                eqTxnIndent + "; WARNING:",
                eqTxnIndent + "; WARNING: " + "The sum of equity transaction is zero without equity account.",
                eqTxnIndent + "; WARNING: " + "Therefore there is no equity posting row, and this is probably not right.",
                eqTxnIndent + "; WARNING: " + "Is account selector correct for this Equity Export?",
                eqTxnIndent + "; WARNING:",
              )
            } else { Nil }) ++
          bs.map(acc => {
            eqTxnIndent + acc.acctn.account + "  " + acc.accountSum.toString() + acc.acctn.commodity.map(c => " " + c.name).getOrElse("")
          }) ++ eqBalRow ++ List("")

      })
    }
  }

  def writeExport(writer: Writer, txnData: TxnData): Unit = {

    val txtEqReport = txnEquity(txnData)
    doRowOutput(writer, txtEqReport)
  }
}
