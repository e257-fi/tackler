/*
 * Copyright 2017 E257.FI
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

import fi.e257.tackler.core.Settings

class RegisterSettings(settings: Settings, myTitle: Option[String], myAccounts: Option[List[String]])
  extends ReportConfiguration {

  val hash = settings.Auditing.hash

  override val minScale = settings.Reports.Register.minScale
  override val maxScale = settings.Reports.Register.maxScale

  val outputname = "reg"

  val title: String = myTitle match {
    case Some(t) => t
    case None => settings.Reports.Register.title
  }

  val accounts: List[String] = myAccounts match {
    case Some(accs) => accs
    case None => settings.Reports.Register.accounts
  }
}

object RegisterSettings {
  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def apply(settings: Settings): RegisterSettings = {
    new RegisterSettings(settings, None, None)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def apply(settings: Settings, myTitle: Option[String], myAccounts: Option[List[String]]): RegisterSettings = {
    new RegisterSettings(settings, myTitle, myAccounts)
  }
}