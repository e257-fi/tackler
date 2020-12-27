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
package fi.e257.tackler.core

import java.nio.file.Paths

import com.typesafe.config.ConfigFactory
import org.scalatest.funspec.AnyFunSpec

class SettingsTest extends AnyFunSpec {

  val scalaVer = util.Properties.versionString.substring(8,12)
   /*
    * "./"  := non-forked JVM
    * "../" := forked JVM
    */
  val testbase = "../"
  val respath = "core/target/scala-" + scalaVer + "/test-classes/"

  describe("Configuration and path handling") {

    it("combine cfg-path with relative basedir") {
      val cfg = Settings(Paths.get(testbase + respath + "cfg-as-ext-file-rel.conf"), ConfigFactory.empty())
      assert(cfg.basedir.endsWith(respath + "cfg/as/ext/file") === true, cfg.basedir)
    }

    it("not change abs basedir ") {
      val cfg = Settings(Paths.get(testbase + respath + "cfg-as-ext-file-abs.conf"), ConfigFactory.empty())
      assert(cfg.basedir === Paths.get("/basedir/as/abs/path/by/ext/conf"))
    }

    it("find embedded config") {
      val cfg = Settings()
      assert(cfg.basedir.endsWith("this/is/tackler_conf") === true, cfg.basedir)
    }

    it("find embedded config with non-exists path") {
      val cfg = Settings(Paths.get("./not/found/config/dir/cfg-is-not-there.conf"), ConfigFactory.empty())
      assert(cfg.basedir.endsWith("this/is/tackler_conf") === true, cfg.basedir)
    }
  }

  describe("generic settings tests") {
    it("rejects invalid zone") {
      val cfg = ConfigFactory.parseString(s"""{ timezone = "Wizard/Oz" }""")
      assertThrows[ConfigurationException]{
        val s = Settings(cfg)
        assert(s.timezone.toString === "never this")
      }
    }

    /**
     * test: 890cf82b-6e7b-4fc3-9d4d-4d8f09156493
     */
    it("Accounts: new and deprecated CoA settings defined at same time") {
      val cfg = ConfigFactory.parseString(s"""{ accounts { coa = [ ], chart-of-accounts = [ ] } }""")
      assertThrows[ConfigurationException]{
        val s = Settings(cfg)
        assert(s.Accounts.coa === "never this")
      }
    }
  }
  /**
   * feature: eb2816e7-7ccf-42a2-9a1a-99223dc431a3
   */
  describe("Min/Max scale configuration") {

    /**
     * feature: a3e8a287-b8ef-41e5-86de-39805fcf729e
     */
    describe("default values") {

      /**
       * test: 1076287b-22f2-4601-8e7e-f2899b71533d
       */
      it("accepts zeros") {
        val cfg = ConfigFactory.parseString("{ reporting { scale { min=0, max=0 } } }")
        val s = Settings(cfg)

        assert(s.Reports.Balance.minScale === 0)
        assert(s.Reports.Balance.maxScale === 0)

        assert(s.Reports.BalanceGroup.minScale === 0)
        assert(s.Reports.BalanceGroup.maxScale === 0)

        assert(s.Reports.Register.minScale === 0)
        assert(s.Reports.Register.maxScale === 0)
      }

      /**
       * test: 02663b5d-1471-471a-befc-5f093e6993ee
       */
      it("accepts valid values") {
        val cfg = ConfigFactory.parseString("{ reporting { scale { min=3, max=10 } } }")
        val s = Settings(cfg)

        assert(s.Reports.Balance.minScale === 3)
        assert(s.Reports.Balance.maxScale === 10)

        assert(s.Reports.BalanceGroup.minScale === 3)
        assert(s.Reports.BalanceGroup.maxScale === 10)

        assert(s.Reports.Register.minScale === 3)
        assert(s.Reports.Register.maxScale === 10)
      }

      /**
       * test: 2cc212bb-f167-4d42-a0e8-8124b3704e1c
       */
      it("doesn't accept negative min") {
        assertThrows[ConfigurationException] {
          val cfg = ConfigFactory.parseString("reporting.scale.min = -1")
          val s = Settings(cfg)
          val _ = s.Reports.Balance.minScale
        }
      }

      /**
       * test: 698ef5a8-2d4c-4d5a-87b1-9df12051e2d7
       */
      it("doesn't accept negative max") {
        assertThrows[ConfigurationException] {
          val cfg = ConfigFactory.parseString("reporting.scale.max = -1")
          val s = Settings(cfg)
          val _ = s.Reports.Balance.maxScale
        }
      }

      /**
       * test: 999044e8-b3e6-447e-a15d-22e23cfdee1b
       */
      it("doesn't accept max < min") {
        assertThrows[ConfigurationException] {
          val cfg = ConfigFactory.parseString("reporting.scale.min = 10, reporting.scale.max = 3")
          val s = Settings(cfg)
          val _ = s.Reports.Balance.maxScale
        }
      }
    }

    /**
     * feature: 07f7efff-e9ec-4e6f-bb21-80ac829d2cda
     */
    describe("balance") {

      /**
       * test: 47d834f5-5d2d-44e9-b42d-58f28a95beb8
       */
      it("uses default values") {
        val s = Settings()
        assert(s.Reports.Balance.minScale === 2)
        assert(s.Reports.Balance.maxScale === 7)
      }

      /**
       * test: 28df6f80-1331-4283-9ea5-8d3101644a9a
       */
      it("accepts report specific values") {
        val cfg = ConfigFactory.parseString("{ reports { balance { scale { min=5, max=9 } } } }")
        val s = Settings(cfg)
        assert(s.Reports.Balance.minScale === 5)
        assert(s.Reports.Balance.maxScale === 9)
      }
    }

    /**
     * feature: 1e80c257-1047-4f4e-91fb-884d3c08add3
     */
    describe("balance-group") {

      /**
       * test: 50a8cf81-0996-4896-b6a1-c6e64083966f
       */
      it("uses default values") {
        val s = Settings()
        assert(s.Reports.BalanceGroup.minScale === 2)
        assert(s.Reports.BalanceGroup.maxScale === 7)
      }

      /**
       * test: f338b972-800f-458a-b607-5ac5ca98ac26
       */
      it("accepts report specific values") {
        val cfg = ConfigFactory.parseString("{ reports { balance-group { scale { min=5, max=9 } } } }")
        val s = Settings(cfg)
        assert(s.Reports.BalanceGroup.minScale === 5)
        assert(s.Reports.BalanceGroup.maxScale === 9)
      }
    }

    /**
     * feature: 9d814986-4b35-4b8d-a662-9b156a31dbc7
     */
    describe("register") {

      /**
       * test: 7db0c6d8-4834-4c33-be82-3a76ef615538
       */
      it("uses default values") {
        val s = Settings()
        assert(s.Reports.Register.minScale === 2)
        assert(s.Reports.Register.maxScale === 7)
      }

      /**
       * test: f8840c32-9165-4539-a4ad-09b421b5d4ec
       */
      it("accepts report specific values") {
        val cfg = ConfigFactory.parseString("{ reports { register { scale { min=5, max=9 } } } }")
        val s = Settings(cfg)
        assert(s.Reports.Register.minScale === 5)
        assert(s.Reports.Register.maxScale === 9)
      }
    }

  }

  /**
   * feature: ed1537a5-494c-4a86-b65c-d2a010297d17
   */
  describe("Hash function") {
    /**
     * test: 1b6876c4-3ef2-43f4-b14b-1bdaa56180fa
     */
    it ("accepts valid jdk8 algorithms") {
      val hashes = List(
        "MD5",
        "SHA-1",
        "SHA-224",
        "SHA-256",
        "SHA-384",
        "SHA-512",
      )
      hashes.foreach(name => {
        val cfg = ConfigFactory.parseString(s"""{ auditing { hash = "$name" } }""")
        val s = Settings(cfg)
        assert(s.Auditing.hash.algorithm === name)
      })
    }

    /**
     * test: 1edf6ced-8bfb-49e8-a307-c05cf7f6cc7e
     */
    it ("rejects invalid algorithms") {
      val hashes = List(
        "MD6",
        "SHA2",
        "SHA256",
        "SHA-257",
      )

      hashes.foreach(name => {
        val cfg = ConfigFactory.parseString(s"""{ auditing { hash = "$name" } }""")
        assertThrows[ConfigurationException] {
          val s = Settings(cfg)
          assert(s.Auditing.hash.algorithm === name) // should never be here
        }
      })
    }
  }
  /**
   * feature: 18e7e5a3-bef5-40a6-a633-31c6b4e41f62
   */
  describe("report-timezone"){
    /**
     * test: 04016f94-3f5c-49cd-b1a2-cbe66af123c5
     */
    it("accepts valid zone name") {
      val cfg = ConfigFactory.parseString(s"""{ reporting { report-timezone = "Europe/Helsinki" } }""")

      val s = Settings(cfg)
      assert(s.Reporting.reportTZ.map(_.toString).getOrElse("") === "Europe/Helsinki")
    }

    /**
     * test: f641ba2e-5159-40c8-b664-9a34c4854898
     */
    it("accepts valid offset") {
      val cfg = ConfigFactory.parseString(s"""{ reporting { report-timezone = "+02:00" } }""")

      val s = Settings(cfg)
      assert(s.Reporting.reportTZ.map(_.toString).getOrElse("") === "+02:00")
    }

    /**
     * test: d4867598-5a40-405b-b14a-e11ba0085d20
     */
    it("rejects invalid zone") {
      val cfg = ConfigFactory.parseString(s"""{ reporting { report-timezone = "Wizard/Oz" } }""")
      assertThrows[ConfigurationException]{
        val s = Settings(cfg)
        assert(s.Reporting.reportTZ.map(_.toString).getOrElse("") === "never this")
      }
    }
    /**
     * test: 4f72b1c5-4d8a-4132-bdfa-2ba23cfff9e2
     */
    it("rejects invalid offset") {
      val cfg = ConfigFactory.parseString(s"""{ reporting { report-timezone = "+19:00" } }""")
      assertThrows[ConfigurationException]{
        val s = Settings(cfg)
        assert(s.Reporting.reportTZ.map(_.toString).getOrElse("") === "never this")
      }
    }
  }

  /**
   * feature: d8d63ca4-9675-4287-ba4e-53b6a329e390
   */
  describe("timestamp-style") {
    /**
     * test: f0e2f23c-7cc6-4610-80c0-8f1e3a6555c7
     */
    it("style: full") {
      val cfg = ConfigFactory.parseString(s"""{ reports { register { timestamp-style = "full" } } }""")
      val s = Settings(cfg)
      assert(s.Reports.Register.tsStyle === FullTsStyle())
    }
    /**
     * test: abec5673-f55e-427d-9db6-cdf865100a21
     */
    it("style: seconds") {
      val cfg = ConfigFactory.parseString(s"""{ reports { register { timestamp-style = "seconds" } } }""")
      val s = Settings(cfg)
      assert(s.Reports.Register.tsStyle === SecondsTsStyle())
    }
    /**
     * test: 0b8dd80a-e826-4107-9892-7db04e3a9f59
     */
    it("style: date") {
      val cfg = ConfigFactory.parseString(s"""{ reports { register { timestamp-style = "date" } } }""")
      val s = Settings(cfg)
      assert(s.Reports.Register.tsStyle === DateTsStyle())
    }
    /**
     * test: fc6c569a-1ab4-4fde-bed6-1593116f617d
     */
    it("rejects invalid style") {
      val cfg = ConfigFactory.parseString(s"""{ reports { register { timestamp-style = "Oz" } } }""")
      assertThrows[ConfigurationException]{
        val s = Settings(cfg)
        assert(s.Reports.Register.tsStyle.toString === "never this")
      }
    }
  }
}
