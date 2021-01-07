/*
 * Copyright 2021 E257.FI
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

import com.typesafe.config.ConfigFactory
import fi.e257.tackler.core.Settings
import org.scalatest.funspec.AnyFunSpecLike

class EquitySettingsTest extends AnyFunSpecLike {
  val settings = Settings()


  describe("EquitySettings") {

    /**
     * test: 70182e6d-cd63-4f36-85be-66778132aace
     */
    it("defaults") {
      val cfg = EquitySettings(settings)

      assert(cfg.equityAccount === "Equity:Balance")
      assert(cfg.accounts === List[String]())
    }

    /**
     * test: 19d938ba-908d-4157-9d63-b33652ce8ace
     */
    it("cfg settings") {
      val eqCfg = ConfigFactory.parseString(
        s"""{ exports { equity { equity-account = "eq:cfg", accounts = [ "cfg:a", "cfg:b" ] } } }""")

      val cfg = EquitySettings(Settings(eqCfg))

      assert(cfg.equityAccount === "eq:cfg")
      assert(cfg.accounts === List[String]("cfg:a", "cfg:b"))
    }

    /**
     * test: b236b5e8-7016-4d72-a413-3254eee3c8c3
     */
    it("apply parameters") {
      val cfg = EquitySettings(settings, Some("Equity:Test"), Some(List("eq-a", "eq-b")))

      assert(cfg.equityAccount === "Equity:Test")
      assert(cfg.accounts === List("eq-a", "eq-b"))
    }
  }
}
