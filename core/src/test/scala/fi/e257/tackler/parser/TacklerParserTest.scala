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

import fi.e257.tackler.core.Settings
import org.scalatest.funspec.AnyFunSpec

class TacklerParserTest extends AnyFunSpec {

  val tt = new TacklerTxns(Settings())

  describe("String Input") {
    /**
     * test: b591d5d3-0be8-4264-8ec0-75b464ff86dc
     */
    it("accept valid input string") {
      val txnStr =
        """
          |2017-01-01 'str
          | e   1
          | a  -1
          |
          |""".stripMargin

      val txnData = tt.string2Txns(txnStr)

      assert(txnData.txns.head.header.description.getOrElse("") === "str")
    }

    /**
     * test: 641c44ab-0ac3-4247-b16e-a4acea5a78ec
     */
    it("accept UFT-8 via string interface") {
      val txnStr =
        """
          |2017-01-01 'äöåÄÖÅéèÿ風空
          | e   1
          | a  -1
          |
          |""".stripMargin

      val txnData = tt.string2Txns(txnStr)
      assert(txnData.txns.head.header.description.getOrElse("") === "äöåÄÖÅéèÿ風空")
    }

    /**
     * test: b0d7d5b1-8927-43c4-80c1-bfda9d0b149f
     */
    it("handle long String input in case of error") {
      val txnStr = """2017-01-01 ()) str""" + " " * 2048

      val ex = intercept[TacklerParseException] {
        tt.string2Txns(txnStr)
      }
      assert(ex.getMessage
        .trim
        .startsWith(
          """Txn Parse Error: Invalid input: truncated inputStr(0, 1024)=[2017-01-01 ()) str""".stripMargin),
        ex.getMessage)
      // This length is fuzzy because total length depends on
      // actual parser error which varies as grammar is changed (while developing it)
      // so let's have some leeway here.
      assert(ex.getMessage.length < 1500)
    }
  }

  describe("Invalid UUID") {

    /**
     * test: 4391990c-83f4-4ea2-8c25-78a87beae219
     */
    it("detect missing uuid") {
      val txnStr =
        """
          |2017-01-01
          | # uuid:
          | e   1
          | a  -1
          |
          |""".stripMargin

      val ex = intercept[TacklerParseException] {
        tt.string2Txns(txnStr)
      }
      assert(ex.getMessage ===
        s"""Txn Parse Error: Invalid input: [
           |2017-01-01
           | # uuid:
           | e   1
           | a  -1
           |
        |], msg:
           |   Can not parse input
           |   on line: 3, at position: 8
           |   no viable alternative at input ' # uuid:\\n'""".stripMargin)
    }

    /**
     * test: 56042ba1-89ca-48da-a55a-d6fea2946c59
     */
    it("notice invalid uuid 1") {

      val txnStr =
        """
          |2017-01-01
          | # uuid: 77356f17-98c9-43c6b9a7-bfc7436b77c8
          | e   1
          | a  -1
          |
          |""".stripMargin
      val ex = intercept[RuntimeException] {
        tt.string2Txns(txnStr)
      }
      assert(ex.getMessage.startsWith("Txn Parse Error: Invalid input: "), ex.getMessage)
    }

    /**
     * test: 08e6dcf3-29b2-44d7-8fb0-af3fc6d74e0c
     */
    it("notice invalid uuid 2") {

      /*
     * https://bugs.openjdk.java.net/browse/JDK-8159339
     * https://bugs.openjdk.java.net/browse/JDK-8165199
     * https://bugs.openjdk.java.net/browse/JDK-8216407
     */
      val txnStr =
        """
          |2017-01-01
          | # uuid: 694aaaaa39222-4d8b-4d0e-8204-50e2a0c8b664
          | e   1
          | a  -1
          |
          |""".stripMargin
      val ex = intercept[RuntimeException] {
        tt.string2Txns(txnStr)
      }
      assert(ex.getMessage.startsWith("Txn Parse Error: Invalid input: "), ex.getMessage)
    }
  }
}
