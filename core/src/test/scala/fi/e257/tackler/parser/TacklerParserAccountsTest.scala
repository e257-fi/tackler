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

import fi.e257.tackler.core.Settings
import org.scalatest.funspec.AnyFunSpec

class TacklerParserAccountsTest extends AnyFunSpec {

  val tt = new TacklerTxns(Settings())

  describe("Account names") {

    /**
     * test: c6584dc1-3a9d-4bb6-8619-0ced9c7c6a17
     */
    it("accept valid non-common account names") {
      val txnStr =
        """
          |2019-01-01
          | e 1
          | a
          |
          |2019-01-01
          | $¢£¤¥ 1
          | a
          |
          |2019-01-01
          | µ 1
          | a
          |
          |2019-01-01
          | ¼½¾⅐Ⅶ 1
          | a
          |
          |2019-01-01
          | ° 1
          | a
          |
          |2019-01-01
          | ¹²³⁴ 1
          | a
          |
          |""".stripMargin

      val txns = tt.string2Txns(txnStr)
      assert(txns.txns.size === 6)
    }

    /**
     * test: 9c836932-718c-491d-8cf0-30e35a0d1533
     */
    it("check invalid sub-account constructs") {
      val perrStrings: List[(String, String, String)] = List(
        (
          // perr: '::'
          """
            |2017-01-01
            | a::b  1
            | e
            |
            |""".stripMargin,
          "on line: 3",
          """at input 'a'"""
        ),
        (
          // perr: :a
          """
            |2017-02-02
            | :a  1
            | e
            |
            |""".stripMargin,
          "on line: 3",
          """at input ' :'"""
        ),
        (
          // perr: a:
          """
            |2017-03-03
            | a:  1
            | e
            |
            |""".stripMargin,
          "on line: 3",
          """at input 'a'"""
        ),
        (
          // perr: '×' U+00D7
          """
            |2017-03-03
            | a×b  1
            | e
            |
            |""".stripMargin,
          "on line: 3",
          """at input '×'"""
        ),
        (
          // perr: '÷' U+00F7
          """
            |2017-03-03
            | a÷b  1
            | e
            |
            |""".stripMargin,
          "on line: 3",
          """at input '÷'"""
        ),
        (
          // perr: ';' U+037E
          """
            |2017-03-03
            | a;b  1
            | e
            |
            |""".stripMargin,
          "on line: 3",
          """at input ';'"""
        )
      )

      val count = perrStrings.map(perrStr => {
        val ex = intercept[TacklerParseException]({
          val _ = TacklerParser.txnsText(perrStr._1)
        })

        assert(ex.getMessage.contains(perrStr._2))
        assert(ex.getMessage.contains(perrStr._3))
        1
      }).foldLeft(0)(_ + _)

      assert(count === 6)
    }
  }

  describe("Numeric accounts names") {
    /**
     * test: 385f7a60-9618-40e4-9f3e-8e28c76a8872
     */
    it("check invalid top-account names") {

      val perrTopAccNames = List(
        """
          |2019-03-14
          | 0a 1
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | 0 1
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | 0:0 1
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | _0 1
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | _0:a 1
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | ·0 1
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | ·0:a 1
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | -0 1
          | s
          |
          |""".stripMargin,
      )

      val count = perrTopAccNames.map(txnStr => {

        val ex = intercept[TacklerParseException]({
          val _ = TacklerParser.txnsText(txnStr)
        })

        assert(ex.getMessage.contains("on line: 3"))
        1
      }).foldLeft(0)(_ + _)

      assert(count === 8)
    }

    /**
     * test: b160ec62-6254-45c8-ac3c-ef0ee41c95b1
     */
    it("check invalid sub-account names") {

      val perrSubAccNames = List(
        """
          |2019-03-14
          | a:0.0 1
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | a:0,0 1
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | a:-0:a 1
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | a:_0 1
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | a:_0:a 1
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | a:·0 1
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | a:·0:a 1
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | a:-0 1
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | a:-0:a 1
          | s
          |
          |""".stripMargin
      )

      val count = perrSubAccNames.map(txnStr => {

        val ex = intercept[TacklerParseException]({
          val _ = TacklerParser.txnsText(txnStr)
        })

        assert(ex.getMessage.contains("on line: 3"))
        1
      }).foldLeft(0)(_ + _)

      assert(count === 9)
    }


    /**
     * test: 78a4af97-a876-4a13-9d67-b7e0ef86ed44
     */
    it("check invalid commodity names") {
      val invalidCommodityNames = List(
        """
          |2019-03-14
          | a 1 0coin
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | a 1 0000
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | a 1 a0.000
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | a 1 a0,000
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | a 1 au:oz
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | a 1 _0
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | a 1 ·0
          | s
          |
          |""".stripMargin,
        """
          |2019-03-14
          | a 1 -0
          | s
          |
          |""".stripMargin,
      )

      val count = invalidCommodityNames.map(txnStr => {

        val ex = intercept[TacklerParseException]({
          val _ = TacklerParser.txnsText(txnStr)
        })

        assert(ex.getMessage.contains("on line: 3"))
        1
      }).foldLeft(0)(_ + _)

      assert(count === 8)
    }
  }
}
