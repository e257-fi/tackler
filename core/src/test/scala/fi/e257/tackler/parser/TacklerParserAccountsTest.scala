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

import org.scalatest.FunSpec


class TacklerParserAccountsTest extends FunSpec {

  val invalidAccountNames = List(
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

  describe ("Numeric accounts names") {

    /**
     * test: 385f7a60-9618-40e4-9f3e-8e28c76a8872
     */
    it("can not affect top-account name rules") {
      val count = invalidAccountNames.map(txnStr => {

        val ex = intercept[TacklerParseException]({
          val _ = TacklerParser.txnsText(txnStr)
        })

        assert(ex.getMessage.contains("on line: 3"))
        1
      }).foldLeft(0)(_ + _)

      assert(count === 17)
    }

    /**
     * test: 78a4af97-a876-4a13-9d67-b7e0ef86ed44
     */
    it("can not affect commodity name rules") {
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
