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
package fi.e257.tackler.parser

import com.typesafe.config.ConfigFactory
import fi.e257.tackler.api.GitInputReference
import fi.e257.tackler.core.{Settings, TacklerException}
import org.scalatest.FunSpec

class TacklerTxnsGitTest extends FunSpec{

  val cfg = ConfigFactory.parseString(
    """
      |{
      |   #  ./ = non-forked JVM
      |   # ../ = forked JVM
      |   basedir = "../tests/audit/"
      |   input {
      |     storage = git
      |     git {
      |       repository = "audit-repo.git"
      |       dir = "txns/2016"
      |       suffix = ".txn"
      |     }
      |   }
      |}
    """.stripMargin)

  val settings = Settings(cfg)

  describe("Git input") {

    /**
     * test: ce2e6523-ee83-46e7-a767-441c5b9f2802
     */
    it("handle ref with 10 (1E1) txns") {
      val tt = new TacklerTxns(settings)
      val txns = tt.git2Txns(TacklerTxns.gitReference("txns-1E1"))

      txns.metadata.get.items.head match {
        case gitmd: GitInputReference => assert(gitmd.commit === "4aa4e9797501c1aefc92f32dff30ab462dae5545")
        case _ => assert(false)
      }

      assert(txns.txns.size === 10)
    }

    /**
     * test: 074f5549-346c-4780-90a1-07d60ae0e79d
     */
    it("handle ref with 100_000 (1E5) txns") {
      val tt = new TacklerTxns(settings)
      val txns = tt.git2Txns(TacklerTxns.gitReference("txns-1E5"))

      txns.metadata.get.items.head match {
        case gitmd: GitInputReference => assert(gitmd.commit === "cb56fdcdd2b56d41fc08cc5af4a3b410896f03b5")
        case _ => assert(false)
      }

      assert(txns.txns.size === 100000)
    }

    /**
     * test: fae31eb0-bd4a-483e-9eb7-9e4c36e7f785
     */
    val loops = 1000
    it(s"survives looping (ref: txns-1E1) x ${loops} loops") {

      val i = (1 to loops).map(_ => {
        val tt = new TacklerTxns(settings)
        val txns = tt.git2Txns(TacklerTxns.gitReference("txns-1E1"))

        txns.metadata.get.items.head match {
          case gitmd: GitInputReference => assert(gitmd.commit === "4aa4e9797501c1aefc92f32dff30ab462dae5545")
          case _ => assert(false)
        }

        assert(txns.txns.size === 10)
        1
      }).sum

      assert(i === loops)
    }

    /**
     * test: a6cfe3b6-feec-4422-afbf-faeca5baf752
     */
    it("reports reasonable details in case of audit error") {

      val auditCfg = ConfigFactory.parseString(
        """
          |{
          |  auditing {
          |    hash = "SHA-256"
          |    txn-set-checksum = on
          |  }
          |}
        """.stripMargin)
        .withFallback(cfg)
        .resolve()

      val auditSettings = Settings(auditCfg)

      val tt = new TacklerTxns(auditSettings)

      val errMsg =
        """GIT: Error while processing git object
          |   commit id: 63014ea235b23aa7330511a25bcba0b62cd33c6f
          |   object id: d87737611e7a2bc551117c77fadd06dbc2c848d8
          |   path: txns/2016/04/01/20160401T115935-25.txn
          |   msg : Configuration setting 'auditing.txn-set-checksum' is activated and there is txn without UUID.""".stripMargin

      val ex = intercept[TacklerException]({
        tt.git2Txns(TacklerTxns.gitReference("errs-1E2"))
      })

      assert(ex.message === errMsg)
    }
  }
}
