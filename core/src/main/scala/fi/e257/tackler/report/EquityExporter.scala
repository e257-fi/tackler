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
package fi.e257.tackler.report
import cats.implicits._
import fi.e257.tackler.api.TxnTS
import fi.e257.tackler.core._
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
      val lastTxn = txnData.txns.last
      val eqTxnHeader = TxnTS.isoZonedTS(lastTxn.header.timestamp) + " " + lastTxn.header.uuid.map(u => "'Equity: last txn (uuid): " + u.toString).getOrElse("'Equity")

      bal.bal.groupBy(b => b.acctn.commStr).flatMap({ case (_, bs) =>
        val eqBalRow = if (bs.map(b => b.accountSum).sum === 0.0) {
          Nil
        } else {
          List(" " + "Equity:Balance")
        }

        List(eqTxnHeader) ++
          bal.metadata.map(md => md.mkString(" ; ", "\n ; ", "")).toList ++
          bs.map(acc => {
            " " + acc.acctn.account + "  " + acc.accountSum.toString() + acc.acctn.commodity.map(c => " " + c.name).getOrElse("")
          }) ++ eqBalRow ++ List("")

      }).toSeq
    }
  }

  def writeExport(writer: Writer, txnData: TxnData): Unit = {

    val txtEqReport = txnEquity(txnData)
    doRowOutput(writer, txtEqReport)
  }
}
