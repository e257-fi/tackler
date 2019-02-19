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
package fi.e257.tackler.model

import com.typesafe.config.ConfigFactory
import fi.e257.tackler.core.{Settings, TxnException}
import fi.e257.tackler.parser.TacklerTxns
import org.scalatest.FunSpec

class TxnDataTest extends FunSpec {

  val uuid01 = "72f7b85b-42ce-4fa2-971e-5ba5fc196d9d"
  val uuid02 = "3a9a2ae9-7aa3-4556-848a-40f5b5e52be6"
  val uuid03 = "dd3bf34b-78e9-4a80-9072-8503c462f7c3"

  val strUUIDs =
    s"""2019-01-01 txn01
       | ;:uuid: ${uuid01}
       | e  1
       | a
       |
       |2019-02-01 txn02
       | ;:uuid: ${uuid02}
       | e  1
       | a
       |
       |2019-03-01 txn03
       | ;:uuid: ${uuid03}
       | e  1
       | a
       |
       |""".stripMargin

  val strNoUUID =
    s"""
       |2019-04-01 txn04
       | e  1
       | a
       |
       |""".stripMargin

  val strDuplicateUUID =
    s"""
       |2019-04-01 txn04
       | ;:uuid: ${uuid02}
       | e  1
       | a
       |
       |""".stripMargin


  val cfg = ConfigFactory.parseString(
    s"""{
       |  auditing {
       |    hash = "SHA-256"
       |    txn-set-checksum = on
       |  }
       |}""".stripMargin)

  val auditSettings = Settings(cfg)

  val ttAudit = new TacklerTxns(auditSettings)
  val ttPlain = new TacklerTxns(Settings())


  val txnsUUIDs = ttAudit.string2Txns(strUUIDs)

  val txnsNoUUID = ttPlain.string2Txns(strNoUUID)

  val txnsDuplicateUUID = ttPlain.string2Txns(strDuplicateUUID)


  describe("Txn Set Checksum") {

    /**
     * test: todo
     */
    it("detects missing uuid from existing txns") {
      val ex = intercept[TxnException] {
        TxnData(Seq.empty, txnsUUIDs.txns ++ txnsNoUUID.txns, Some(auditSettings))
      }
      assert(ex.message.startsWith(
        "Found missing txn uuid with txn set checksum"
      ))
    }

    /**
     * test: todo
     */
    it("detects duplicate uuid from existing txns") {
      val ex = intercept[TxnException] {
        TxnData(Seq.empty, txnsDuplicateUUID.txns ++ txnsUUIDs.txns ++ txnsDuplicateUUID.txns, Some(auditSettings))
      }

      assert(ex.message.startsWith(
        "Found 3 duplicate txn uuids with txn set checksum. At least \"3a9a2ae9-7aa3-4556-848a-40f5b5e52be6\""))
    }
  }
}
