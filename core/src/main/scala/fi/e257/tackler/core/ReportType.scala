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
package fi.e257.tackler.core

import fi.e257.tackler.report.Writers

sealed trait ReportFormat {
  val ext: String

  /**
   * Add console output, if it is supported by this format
   * @param writers
   * @param consoles
   * @return
   */
  def consoleOutput(writers: Writers, consoles: Writers): Writers = {
    writers ++ consoles
  }
}

sealed case class TextFormat() extends ReportFormat {
  val ext: String = "txt"
}

sealed case class JsonFormat() extends ReportFormat {
  val ext: String = "json"

  override
  def consoleOutput(writers: Writers, consoles: Writers): Writers = writers
}

object ReportFormat {
  def apply(format: String): ReportFormat = {
    format match {
      case CfgValues.txt => TextFormat()
      case CfgValues.json => JsonFormat()

      /* Error*/
      case frmt => throw new ReportException(
        "Unknown report format [" + frmt + "]. Valid formats are: " + CfgValues.txt + ", " + CfgValues.json)
    }
  }
}


sealed trait OutputType
sealed trait ReportType extends OutputType
sealed trait ExportType extends OutputType

sealed case class BalanceReportType() extends ReportType
sealed case class BalanceGroupReportType() extends ReportType
sealed case class RegisterReportType() extends ReportType

sealed case class EquityExportType() extends ExportType
sealed case class IdentityExportType() extends ExportType

object ReportType {
  def apply(groupBy: String): ReportType = {
    groupBy match {
      case CfgValues.balance => BalanceReportType()
      case CfgValues.balanceGroup => BalanceGroupReportType()
      case CfgValues.register => RegisterReportType()
      /* Error*/
      case rpt => throw new ReportException(
        "Unknown report type [" + rpt + "]. Valid types are: " +
          CfgValues.balance + ", " +
          CfgValues.balanceGroup + ", " +
          CfgValues.register)
    }
  }
}

object ExportType {
  def apply(groupBy: String): ExportType = {
    groupBy match {
      case CfgValues.equity => EquityExportType()
      case CfgValues.identity => IdentityExportType()
      /* Error*/
      case xpt => throw new ExportException(
        "Unknown export type [" + xpt + "]. Valid types are: " + CfgValues.equity + ", " + CfgValues.identity)
    }
  }
}
