/*
 * Copyright 2017-2019 E257.FI
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

import io.circe.optics.JsonPath
import fi.e257.tackler.api.{BalanceGroupReport, BalanceReport, RegisterReport, TxnHeader}
import fi.e257.tackler.core.{GroupByIsoWeek, Settings}
import fi.e257.tackler.parser.TacklerTxns
import org.scalatest.flatspec.AnyFlatSpec

class ReportApiTest extends AnyFlatSpec {

  val settings = Settings()
  val txnStr =
    """2017-12-14 'txn-01
    | e:e14  14
    | a
    |
    |2017-12-15 'txn-02
    | x:not·this  15
    | a
    |
    |2017-12-16 'txn-03
    | e:e16  16
    | a
    |
    |""".stripMargin

  val tt = new TacklerTxns(settings)
  val txnData = tt.string2Txns(txnStr)


  val _title = JsonPath.root.title.string


  behavior of "Balance report API"
  val _accountTreeSum = JsonPath.root.balances.index(0).accountTreeSum.string
  val _delta = JsonPath.root.deltas.index(0).delta.string

  /**
   *  test: f003a816-3107-4398-902f-656479cf1ee5
   */
  it must "work with default settings" in {
    val balSettings = BalanceSettings(settings)
    val rpt = new BalanceReporter(balSettings)

    val report = rpt.jsonReport(txnData)

    assert(_title.getOption(report) === Some("BALANCE"))
    assert(_accountTreeSum.getOption(report) === Some("-45.00"))
    assert(_delta.getOption(report) === Some("0.00"))

    val foo = report.as[BalanceReport]
    assert(JsonHelper.getBalanceReport(foo).map(_.title) === Some("BALANCE"))
  }

  /**
   * test: a50327c0-bd16-42a8-82e4-0846be4b5c6f
   */
  it must "accept arguments" in {
    val balCfg = BalanceSettings(settings, Some("Test-Balance"), Some(List("^e.*", "^a.*")))
    val rpt = new BalanceReporter(balCfg)

    val report = rpt.jsonReport(txnData)

    assert(_title.getOption(report) === Some("Test-Balance"))
    assert(_accountTreeSum.getOption(report) === Some("-45.00"))
    assert(_delta.getOption(report) === Some("-15.00"))

    val foo = report.as[BalanceReport]
    assert(JsonHelper.getBalanceReport(foo).map(_.title) === Some("Test-Balance"))
  }


  behavior of "BalanceGroup report API"
  val _balgrp_title = JsonPath.root.groups.index(0).title.string
  val _balgrp_accountTreeSum = JsonPath.root.groups.index(0).balances.index(0).accountTreeSum.string
  val _balgrp_delta = JsonPath.root.groups.index(0).deltas.index(0).delta.string

  /**
   * test: d6fe5451-2d5d-4ced-848a-934fbc5e43ab
   */
  it must "work with default settings" in {
    val balGrpCfg = BalanceGroupSettings(settings)
    val rpt = new BalanceGroupReporter(balGrpCfg)

    val report = rpt.jsonReport(txnData)

    assert(_title.getOption(report) === Some("BALANCE GROUPS"))
    assert(_balgrp_title.getOption(report) === Some("2017-12 Z"))
    assert(_balgrp_accountTreeSum.getOption(report) === Some("-45.00"))
    assert(_balgrp_delta.getOption(report) === Some("0.00"))

    val foo = report.as[BalanceGroupReport]
    assert(JsonHelper.getBalanceGroupReport(foo).map(_.title) === Some("BALANCE GROUPS"))
  }

  /**
   * test: f1f8a1ac-452b-47df-b15b-9e9bf176028a
   */
  it must "accept arguments" in {
    val balGrpCfg = BalanceGroupSettings(settings, Some("Test-BalGrp"), Some(List("^e.*", "^a.*")), Some(GroupByIsoWeek()))
    val rpt = new BalanceGroupReporter(balGrpCfg)

    val report = rpt.jsonReport(txnData)

    assert(_title.getOption(report) === Some("Test-BalGrp"))
    assert(_balgrp_title.getOption(report) === Some("2017-W50 Z"))
    assert(_balgrp_accountTreeSum.getOption(report) === Some("-45.00"))
    assert(_balgrp_delta.getOption(report) === Some("-15.00"))

    val foo = report.as[BalanceGroupReport]
    assert(JsonHelper.getBalanceGroupReport(foo).map(_.title) === Some("Test-BalGrp"))
  }


  behavior of "Register report API"
  val _reg_txn_idx1_desc = JsonPath.root.transactions.index(1).txn.description.string

  /**
   * test: 12f73e1a-b96c-43da-8031-30765943bc4f
   */
  it must "work with default settings" in {
    val regCfg = RegisterSettings(settings)
    val rpt = new RegisterReporter(regCfg)

    val report = rpt.jsonReport(txnData)

    assert(_title.getOption(report) === Some("REGISTER"))
    assert(_reg_txn_idx1_desc.getOption(report) === Some("txn-02")) // no filter

    val foo = report.as[RegisterReport]
    assert(JsonHelper.getRegisterReport(foo).map(_.title) === Some("REGISTER"))
  }

  /**
   * test: 71b2e53b-57bc-4f6a-8ad8-4864fd370884
   */
  it must "accepts arguments" in {
    val regCfg = RegisterSettings(settings, Some("Test-Register"), Some(List("^e.*")))
    val rpt = new RegisterReporter(regCfg)

    val report = rpt.jsonReport(txnData)

    assert(_title.getOption(report) === Some("Test-Register"))
    assert(_reg_txn_idx1_desc.getOption(report) === Some("txn-03")) // with filter

    val foo = report.as[RegisterReport]
    assert(JsonHelper.getRegisterReport(foo).map(_.title) === Some("Test-Register"))
  }

  /**
    * test: 04d83aba-4d19-4add-bff4-b79180b8b726
    */
  it must "metadata: uuid" in {
    val uuidTxnStr =
      """
        |2019-01-01 'uuid = 78436575-3613-483d-a7ed-d9917b1d5c80
        | # uuid: 78436575-3613-483d-a7ed-d9917b1d5c80
        | e 1
        | a
        |
        |""".stripMargin

    val uuidTxnData = tt.string2Txns(uuidTxnStr)

    val regCfg = RegisterSettings(settings, Some("UUID"), None)
    val reporter = new RegisterReporter(regCfg)

    //
    // Report -> JSON
    //
    val jsonRpt = reporter.jsonReport(uuidTxnData)

    val _reg_txn_idx0_desc = JsonPath.root.transactions.index(0).txn.description.string
    val _reg_txn_idx0_uuid = JsonPath.root.transactions.index(0).txn.uuid.string

    assert(_reg_txn_idx0_desc.getOption(jsonRpt) === Some("uuid = 78436575-3613-483d-a7ed-d9917b1d5c80"))
    assert(_reg_txn_idx0_uuid.getOption(jsonRpt) === Some("78436575-3613-483d-a7ed-d9917b1d5c80"))

    //
    // JSON -> Report
    //
    val jsonResult = jsonRpt.as[RegisterReport]
    assert(jsonResult.isRight)

    val rptFromJson = JsonHelper.getRegisterReport(jsonResult).get
    assert(rptFromJson.title === "UUID")

    assert(rptFromJson.transactions.head.txn.description.get.toString === "uuid = 78436575-3613-483d-a7ed-d9917b1d5c80")
    assert(rptFromJson.transactions.head.txn.uuid.get.toString === "78436575-3613-483d-a7ed-d9917b1d5c80")
  }

  /**
    * test: f3409965-68ae-4964-a73b-e46e0a2d8304
    */
  it must "metadata: location" in {

    val geoTxnStr =
      """
        |2019-02-02 'geo = geo:61,25.1,2
        | # location: geo:61,25.1,2
        | e 1
        | a
        |
        |""".stripMargin

    val geoTxnData = tt.string2Txns(geoTxnStr)

    val regCfg = RegisterSettings(settings, Some("location"), None)
    val reporter = new RegisterReporter(regCfg)

    //
    // Report -> JSON
    //
    val jsonRpt = reporter.jsonReport(geoTxnData)

    val _reg_txn_idx0_desc         = JsonPath.root.transactions.index(0).txn.description.string
    val _reg_txn_idx0_location_lat = JsonPath.root.transactions.index(0).txn.location.lat.double
    val _reg_txn_idx0_location_lon = JsonPath.root.transactions.index(0).txn.location.lon.double
    val _reg_txn_idx0_location_alt = JsonPath.root.transactions.index(0).txn.location.alt.double

    assert(_reg_txn_idx0_desc.getOption(jsonRpt) === Some("geo = geo:61,25.1,2"))

    assert(_reg_txn_idx0_location_lat.getOption(jsonRpt) === Some(61))
    assert(_reg_txn_idx0_location_lon.getOption(jsonRpt) === Some(25.1))
    assert(_reg_txn_idx0_location_alt.getOption(jsonRpt) === Some(2))

    //
    // JSON -> Report
    //
    val jsonResult = jsonRpt.as[RegisterReport]
    assert(jsonResult.isRight)

    val rptFromJson = JsonHelper.getRegisterReport(jsonResult).get
    assert(rptFromJson.title === "location")

    val txnHdr: TxnHeader = rptFromJson.transactions.head.txn

    assert(txnHdr.description.get.toString === "geo = geo:61,25.1,2")
    assert(txnHdr.location.get.lat === 61)
    assert(txnHdr.location.get.lon === 25.1)
    assert(txnHdr.location.get.alt === Some(2))
  }
}
