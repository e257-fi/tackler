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

import fi.e257.tackler.core.Settings
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class BalanceSettingsTest extends AnyFlatSpec with Matchers {
  val settings = Settings()

  behavior of "BalanceSettings"

  it should "apply with default" in {
    val cfg = BalanceSettings(settings)

    cfg.minScale mustBe 2
    cfg.maxScale mustBe 7

    cfg.title mustBe "BALANCE"
    cfg.accounts mustBe List[String]()
  }

  it should "apply" in {
    val cfg = BalanceSettings(settings, Some("unit test"), Some(List("a", "b")))

    cfg.minScale mustBe 2
    cfg.maxScale mustBe 7

    cfg.title mustBe "unit test"
    cfg.accounts mustBe List("a", "b")
  }
}
