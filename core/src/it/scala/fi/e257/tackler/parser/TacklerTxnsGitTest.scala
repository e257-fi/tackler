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

class TacklerTxnsGitTest extends FunSpec {

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

  describe("Git storage system") {

    describe("Normal operations") {

      /**
       * test: ce2e6523-ee83-46e7-a767-441c5b9f2802
       */
      it("handled ref with 10 (1E1) txns") {
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
      it("handled ref with 100_000 (1E5) txns") {
        val tt = new TacklerTxns(settings)
        val txns = tt.git2Txns(TacklerTxns.gitReference("txns-1E5"))

        txns.metadata.get.items.head match {
          case gitmd: GitInputReference => assert(gitmd.commit === "cb56fdcdd2b56d41fc08cc5af4a3b410896f03b5")
          case _ => assert(false)
        }

        assert(txns.txns.size === 100000)
      }
    }


    describe("Test looping with 10 txns") {
      val loops = 10000

      /**
       * test: fae31eb0-bd4a-483e-9eb7-9e4c36e7f785
       */
      it(s"made ${loops} loops with txns-1E1") {

        val loopCount = (1 to loops).foldLeft((0, 0)) { case (i, r) => {
          val tt = new TacklerTxns(settings)
          val txns = tt.git2Txns(TacklerTxns.gitReference("txns-1E1"))

          txns.metadata.get.items.head match {
            case gitmd: GitInputReference => assert(gitmd.commit === "4aa4e9797501c1aefc92f32dff30ab462dae5545")
            case _ => assert(false)
          }

          assert(txns.txns.size === 10)

          val printRound = r / (loops / 10)
          if (i._2 < printRound) {
            System.err.println(f"Done: ${r}%10d of $loops loops")
            (i._1 + 1, printRound)
          } else {
            (i._1 + 1, i._2)
          }
        }
        }

        assert(loopCount._1 === loops)
      }
    }

    describe("Test looping with 100_000 txns") {
      /**
       * test: 33d85471-a04c-49b9-b7a0-9d7f7f5762eb
       */
      it("made 10 loops with txns-1E5") {

        val loopCount = (1 to 10).foldLeft((0, 0.0)) { case (i, _) => {
          val tt = new TacklerTxns(settings)

          val tsStart = System.currentTimeMillis()
          val txns = tt.git2Txns(TacklerTxns.gitReference("txns-1E5"))
          val tsEnd = System.currentTimeMillis()

          txns.metadata.get.items.head match {
            case gitmd: GitInputReference => assert(gitmd.commit === "cb56fdcdd2b56d41fc08cc5af4a3b410896f03b5")
            case _ => assert(false)
          }

          assert(txns.txns.size === 100000)

          val txn_s = 100000.0 / ((tsEnd - tsStart) / 1000.0)
          System.err.println(f"Done: ${i._1 + 1}%10d of 10 loops, ${txn_s}%.0f txn/s")
          (i._1 + 1, i._2 + txn_s)
        }
        }

        val txn_s_ave = loopCount._2 / 10
        val txn_s_ref = 31000
        System.err.println(f"On average: ${txn_s_ave}%.0f txn/s. " +
          f"Reference system (laptop): ${txn_s_ref} txn/s (${txn_s_ave - txn_s_ref}%+.0f txn/s)")
        assert(loopCount._1 === 10)
      }
    }

    describe("Error cases") {
      /**
       * test: a6cfe3b6-feec-4422-afbf-faeca5baf752
       */
      it("reported reasonable details in case of audit error") {

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
}
