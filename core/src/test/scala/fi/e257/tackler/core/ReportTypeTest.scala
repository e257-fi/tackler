/*
 * Copyright 2017-2020 E257.FI
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
package fi.e257.tackler.core

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class ReportTypeTest extends AnyFlatSpec with Matchers {
  behavior of "ReportType"

  it should "accept balance" in {
    ReportType(CfgValues.balance) mustBe a[BalanceReportType]
  }
  it should "accept balance-group" in {
    ReportType(CfgValues.balanceGroup) mustBe a[BalanceGroupReportType]
  }
  it should "accept register" in {
    ReportType(CfgValues.register) mustBe a[RegisterReportType]
  }

  it should "not accepts (equity)" in {
    assertThrows[ReportException]{
      ReportType(CfgValues.equity)
    }
  }
  it should "not accept export (identity)" in {
    assertThrows[ReportException]{
      ReportType(CfgValues.identity)
    }
  }
}

class ExportTypeTest extends AnyFlatSpec with Matchers {

  behavior of "ExportType"

  it should "accept equity" in {
    ExportType(CfgValues.equity) mustBe a[EquityExportType]
  }

  it should "accept identity" in {
    ExportType(CfgValues.identity) mustBe a[IdentityExportType]
  }

  it should "not accept reports (balance)" in {
    assertThrows[ExportException]{
      ExportType(CfgValues.balance)
    }
  }
  it should "not accept reports (balance-group)" in {
    assertThrows[ExportException]{
      ExportType(CfgValues.balanceGroup)
    }
  }
  it should "not accept reports (register)" in {
    assertThrows[ExportException]{
      ExportType(CfgValues.register)
    }
  }
}
