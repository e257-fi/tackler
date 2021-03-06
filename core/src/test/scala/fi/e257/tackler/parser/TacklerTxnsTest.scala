/*
 * Copyright 2016-2019 E257.FI
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

import fi.e257.tackler.core.{Settings, TxnException}
import org.scalatest.funspec.AnyFunSpec

class TacklerTxnsTest extends AnyFunSpec {

  describe("TacklerTxns with String") {

    it("create git commitId by string") {
      assert(TacklerTxns.gitCommitId("1234567890") === Right[String, String]("1234567890"))
    }

    it("create git ref by settings") {
      val settings = Settings()
      assert(TacklerTxns.gitReference(settings) === Left[String, String]("master"))
    }

    it("create git ref by string") {
      assert(TacklerTxns.gitReference("unit-test-ref") === Left[String, String]("unit-test-ref"))
    }

    /**
     * test: 52836ff9-94de-4575-bfae-6b5afa971351
     */
    it("notice unbalanced transaction") {
      val txnStr =
        """2017-01-01
          | e  1
          | a  1
          |""".stripMargin

      val ex = intercept[TxnException] {
        val tt = new TacklerTxns(Settings())

        tt.string2Txns(txnStr)
      }
      assert(ex.getMessage === "TXN postings do not zero: 2")
    }

    /**
     * test: 200aad57-9275-4d16-bdad-2f1c484bcf17
     */
    it("handle multiple txns") {
      val txnStr =
        """2017-01-03 'str3
          | e  1
          | a
          |
          |2017-01-01 'str1
          | e  1
          | a
          |
          |2017-01-02 'str2
          | e  1
          | a
          |
          |""".stripMargin

      val tt = new TacklerTxns(Settings())

      val txnData = tt.string2Txns(txnStr)
      assert(txnData.txns.length === 3)
      assert(txnData.txns.head.header.description.getOrElse("") === "str1")
      assert(txnData.txns.last.header.description.getOrElse("") === "str3")
    }
  }
}
