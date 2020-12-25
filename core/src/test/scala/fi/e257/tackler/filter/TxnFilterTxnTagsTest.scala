/*
 * Copyright 2020 E257.FI
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
package fi.e257.tackler.filter

import fi.e257.tackler.api._
import fi.e257.tackler.core.Settings
import fi.e257.tackler.parser.TacklerTxns
import org.scalatest.funspec.AnyFunSpec

class TxnFilterTxnTagsTest extends AnyFunSpec with TxnFilterSpec {
  val tt = new TacklerTxns(Settings())

  val uuidTxnEr = "66c7451b-d1e1-4d51-9840-ccbfcf1b0624"
  val uuidTxn01 = "51d16fed-9e92-49ed-9c4a-3dbf3755f940"
  val uuidTxn02 = "1bde4709-f2c5-48cd-bfb7-0f4c0b7f126c"
  val uuidTxn03 = "86678dbf-5759-492f-8e8c-5af26f204114"
  val uuidTxn04 = "8e41a20c-6915-42fb-9014-576527ddab18"
  val uuidTxn05 = "26ceb3e8-ab9f-490b-948c-33b34b7a0a60"

  val txnStr =
    s"""2020-12-24 'abc
       | # uuid: ${uuidTxnEr}
       | e:b  1
       | a
       |
       |2020-12-24 (abc) 'txn02
       | # uuid: ${uuidTxnEr}
       | e:b  1 ; abc hamburger
       | a
       |
       |2020-12-24 (xyz) 'txn03
       | # uuid: ${uuidTxnEr}
       | ; abc
       | e:b  2 EUR
       | a
       |
       |2020-12-24
       | # uuid: ${uuidTxnEr}
       | e 3
       | abc
       |
       |2020-12-24
       | # uuid: ${uuidTxnEr}
       | e 4 abc
       | a
       |
       |2020-12-24
       | # uuid: ${uuidTxn01}
       | # tags: abc
       | e 1
       | a
       |
       |2020-12-24
       | # uuid: ${uuidTxn02}
       | # tags: abc, def
       | e 1
       | a
       |
       |2020-12-24
       | # uuid: ${uuidTxn03}
       | # tags: abc, def, tuv
       | e 1
       | a
       |
       |""".stripMargin

  val txnsAll = tt.string2Txns(txnStr)


  /**
   * test: f3d05712-3c6e-482c-bfb9-8b559b8f6eb9
   */
  describe("Transaction Tags filters") {

    it("filters by basic tag 1") {
      val txnFilter =TxnFilterTxnTags("abc")

      val txnData = txnsAll.filter(TxnFilterDefinition(txnFilter))
      assert(noErrors(txnData, uuidTxnEr))

      assert(txnData.txns.size === 3)
      assert(checkUUID(txnData, uuidTxn01))
      assert(checkUUID(txnData, uuidTxn02))
      assert(checkUUID(txnData, uuidTxn03))
    }

    it("filters by basic tag 2") {
      val txnFilter =TxnFilterTxnTags("def")

      val txnData = txnsAll.filter(TxnFilterDefinition(txnFilter))
      assert(noErrors(txnData, uuidTxnEr))

      assert(txnData.txns.size === 2)
      assert(checkUUID(txnData, uuidTxn02))
      assert(checkUUID(txnData, uuidTxn03))
    }

    it("filters by basic tag 3") {
      val txnFilter =TxnFilterTxnTags("tuv")

      val txnData = txnsAll.filter(TxnFilterDefinition(txnFilter))
      assert(noErrors(txnData, uuidTxnEr))

      assert(txnData.txns.size === 1)
      assert(checkUUID(txnData, uuidTxn03))
    }

    it("filters by regex") {
      val txnFilter =TxnFilterTxnTags(".*e.*")

      val txnData = txnsAll.filter(TxnFilterDefinition(txnFilter))
      assert(noErrors(txnData, uuidTxnEr))

      assert(txnData.txns.size === 2)
      assert(checkUUID(txnData, uuidTxn02))
      assert(checkUUID(txnData, uuidTxn03))
    }
  }
}
