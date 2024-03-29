/*
 * Copyright 2019 E257.FI
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

import fi.e257.tackler.api.{BalanceGroupReport, BalanceReport, RegisterReport}
import io.circe

object JsonHelper {
  def getBalanceReport(json: Either[circe.Error, BalanceReport]): Option[BalanceReport] = {
    json.toOption
  }
  def getBalanceGroupReport(json: Either[circe.Error, BalanceGroupReport]): Option[BalanceGroupReport] = {
    json.toOption
  }
  def getRegisterReport(json: Either[circe.Error, RegisterReport]): Option[RegisterReport] = {
    json.toOption
  }
}
