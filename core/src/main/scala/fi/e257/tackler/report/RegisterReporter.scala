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

import java.time.ZonedDateTime

import io.circe.Json
import io.circe.syntax._
import fi.e257.tackler.api.{Metadata, RegisterPosting, RegisterReport, RegisterTxn, TimeZoneInfo, TxnTS}
import fi.e257.tackler.core._
import fi.e257.tackler.model.{RegisterEntry, _}

class RegisterReporter(val mySettings: RegisterSettings) extends ReportLike(mySettings) {

  private val regHdrTsOp: (ZonedDateTime => String) = mySettings.reportTZ match {
    case Some(reportTZ) =>
      mySettings.timestampStyle match {
        case DateTsStyle() => { txnTs: ZonedDateTime => TxnTS.localDate(txnTs, reportTZ) }
        case SecondsTsStyle() => { txnTs: ZonedDateTime => TxnTS.localSeconds(txnTs, reportTZ) }
        case FullTsStyle() => { txnTs: ZonedDateTime => TxnTS.localFull(txnTs, reportTZ) }
      }
    case None => {
      mySettings.timestampStyle match {
        case DateTsStyle() => { txnTs: ZonedDateTime => TxnTS.tzDate(txnTs) }
        case SecondsTsStyle() => { txnTs: ZonedDateTime => TxnTS.tzSeconds(txnTs) }
        case FullTsStyle() => { txnTs: ZonedDateTime => TxnTS.tzFull(txnTs) }
      }
    }
  }


  override val name = mySettings.outputname

  protected def txtRegisterEntry(regEntry: RegisterEntry, regEntryPostings: Seq[AccumulatorPosting]): Seq[String] = {

    val txn = regEntry._1

    val indent = " " * 12

    val txtRegTxnHeader: String = txn.txnHeaderToString(indent, regHdrTsOp)

    val txtRegPostings = regEntryPostings
      .map(regPosting => {
        indent + "%-33s".format(regPosting.account) +
          fillFormat(18, regPosting.amount) + " " + fillFormat(18, regPosting.runningTotal) +
          regPosting.commodity.map(c => " " + c.name).getOrElse("")
      })

    if (txtRegPostings.nonEmpty) {
      val maxLength = txtRegPostings
        .foldLeft(0)({ case (l, s) =>
          if (l < s.length) s.length
          else l
        })

      val sep = "-" * maxLength
      List(txtRegTxnHeader + txtRegPostings.mkString("\n") + "\n" + sep)
    } else {
      Nil
    }
  }

  protected def jsonRegisterEntry(registerEntry: RegisterEntry, regEntryPostings: Seq[AccumulatorPosting]): Seq[RegisterTxn] = {

    if (regEntryPostings.isEmpty) {
      Seq.empty[RegisterTxn]
    }
    else {
      val txn = registerEntry._1

      val reportPostings = regEntryPostings
        .map(regPosting => {
          RegisterPosting(
            account = regPosting.account,
            amount = scaleFormat(regPosting.amount),
            runningTotal = scaleFormat(regPosting.runningTotal),
            commodity = regPosting.commodity.map(_.name)
          )
        })

      List(RegisterTxn(txn.header, reportPostings))
    }
  }

  protected def txtRegisterReport(metadata: Option[Metadata], accounts: RegisterAccountSelector, txns: TxnData): Seq[String] = {

    val header = List(
      metadata.fold("") { md => md.text() },
      mySettings.title,
      "-" * mySettings.title.length)


    val body = Accumulator.registerStream[String](txns.txns, accounts)({ (regEntry: RegisterEntry) =>
      val regEntryPostings = regEntry._2
      val txtRegEntry = txtRegisterEntry(regEntry, regEntryPostings)

      txtRegEntry

    })
    if (body.isEmpty) {
      header
    } else {
      header ++ body
    }
  }

  protected def doBody(accounts: Filtering[AccumulatorPosting], txns: Txns): Seq[RegisterTxn] = {

    val a = Accumulator.registerStream[RegisterTxn](txns, accounts)({ (regEntry: RegisterEntry) =>
      val regEntryPostings = regEntry._2
      jsonRegisterEntry(regEntry, regEntryPostings)
    })

    a
  }

  protected def jsonRegisterReport(metadata: Option[Metadata] , accounts: RegisterAccountSelector, txns: TxnData): Json = {

    RegisterReport(metadata, mySettings.title, doBody(accounts, txns.txns)).asJson
  }

  protected def getFilters() = {
    if (mySettings.accounts.isEmpty) {
      new AllRegisterPostings(mySettings.hash)
    } else {
      new RegisterFilterByAccount(mySettings.accounts, mySettings.hash)
    }
  }

  override
  def jsonReport(txns: TxnData): Json = {
    val rrf = getFilters()
    // There is no TimeZoneInfo element, because there isn't any time manipulation done with JSON report
    val md = txns.getMetadata(rrf)

    jsonRegisterReport(md, rrf, txns)
  }

  override
  def writeReport(formats: Formats, txns: TxnData): Unit = {
    val rrf = getFilters()
    val md = txns.getMetadata(rrf)

    formats.foreach({ case (format, writers) =>
      format match {
        case TextFormat() => {
          doRowOutputs(writers, txtRegisterReport(Metadata.append(md, TimeZoneInfo(mySettings.reportTZ)), rrf, txns))
        }
        case JsonFormat() => {
          // There is no TimeZoneInfo element, because there isn't any time manipulation done with JSON report
          doRowOutputs(writers, Seq(jsonRegisterReport(md, rrf, txns).printWith(printer)))
        }
      }
    })
  }
}
