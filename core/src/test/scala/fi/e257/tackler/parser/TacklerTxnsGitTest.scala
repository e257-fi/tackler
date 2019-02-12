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
import fi.e257.tackler.core.Settings
import org.scalatest.FunSpec

class TacklerTxnsGitTest extends FunSpec{

  val cfg = ConfigFactory.parseString(
    """
      |{
      |   basedir = "core/src/test/resources"
      |   input {
      |     storage = git
      |     git {
      |       repository = "git-scalatest-repo.git"
      |       dir = "2016"
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
    it("handle ref with 1E3 txns") {
      val tt = new TacklerTxns(settings)
      val txns = tt.git2Txns(TacklerTxns.gitReference("txns-1E3"))

      txns.metadata.get.metadataItems.head match {
        case gitmd: GitInputReference => assert(gitmd.commit === "6fd9d4d31910d9960470413823ec0b96dc2e70ac")
        case _ => assert(false)
      }

      assert(txns.txns.size === 1000)
    }

    /**
     * test: 074f5549-346c-4780-90a1-07d60ae0e79d
     */
    it("handle ref with 1E5 txns") {
      val tt = new TacklerTxns(settings)
      val txns = tt.git2Txns(TacklerTxns.gitReference("txns-1E5"))

      txns.metadata.get.metadataItems.head match {
        case gitmd: GitInputReference => assert(gitmd.commit === "75ad8168da60b23541f7c2eb29c2ab4a8293ed8c")
        case _ => assert(false)
      }

      assert(txns.txns.size === 100000)
    }

    /**
     * test: fae31eb0-bd4a-483e-9eb7-9e4c36e7f785
     */
    it("survive looping") {

      val loops = 10

      var i = 0
      for (_ <- 1 to loops) {
        val tt = new TacklerTxns(settings)
        val txns = tt.git2Txns(TacklerTxns.gitReference("txns-1E3"))

        txns.metadata.get.metadataItems.head match {
          case gitmd: GitInputReference => assert(gitmd.commit === "6fd9d4d31910d9960470413823ec0b96dc2e70ac")
          case _ => assert(false)
        }

        assert(txns.txns.size === 1000)
        i += 1
      }
      assert(i === loops)
    }
  }
}
