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
import org.scalatest.FunSpec

class TacklerParserLocationTest extends FunSpec {

  val tt = new TacklerTxns(Settings())

  describe("Geo URI tests") {

    /**
      * test:
      *
      */
    it("pok: geo uris") {
      val txnStr =
        """
          |2019-04-01
          | # location: geo:60.170833,24.9375
          | e 1
          | a
          |
          |2019-04-01
          | # location: geo:66.5436,25.84715,160
          | e 1
          | a
          |
          |2019-04-01
          | # location: geo:66.5436,25.84715,160.0
          | e 1
          | a
          |
          |2019-04-01
          | # location: geo:59.90735,16.57532,-155
          | e 1
          | a
          |
          |2019-04-01
          | # location: geo:59.90735,16.57532,-155.0
          | e 1
          | a
          |
          |2019-04-01
          | # location: geo:0,0,0
          | e 1
          | a
          |
          |2019-04-01
          | # location: geo:-90,0,0
          | e 1
          | a
          |
          |2019-04-01
          | # location: geo:-90,25,0
          | e 1
          | a
          |
          |2019-04-01
          | # location: geo:90,0,0
          | e 1
          | a
          |
          |2019-04-01
          | # location: geo:90,25,0
          | e 1
          | a
          |
          |2019-04-01
          | # location: geo:66.56,180,0
          | e 1
          | a
          |
          |2019-04-01
          | # location: geo:-66.56,-180,0
          | e 1
          | a
          |
          |""".stripMargin

      val txns = tt.string2Txns(txnStr)
      assert(txns.txns.size === 12)
    }

    /**
      * test:
      */
    it("perr: detect invalid geo uris") {
      val perrStrings: List[(String, String, String)] = List(
        (
          """
            |2017-01-01
            | # location:
            | e 1
            | a
            |
            |""".stripMargin,
          "on line: 3",
          s"""at input '"""
        ),
        (
          // perr: no 'geo'
          """
            |2017-01-01
            | # location: 60.170833,24.9375
            | e 1
            | a
            |
            |""".stripMargin,
          "on line: 3",
          s"""at input '"""
        ),
        (
          // perr: decimal ','
          """
            |2017-01-01
            | # location: geo:0.0,0.0,0,0
            | e 1
            | a
            |
            |""".stripMargin,
          "on line: 3",
          """at input ' '"""
        ),
        (
          // perr: missing lat/lon
          """
            |2017-01-01
            | # location: geo:0
            | e 1
            | a
            |
            |""".stripMargin,
          "on line: 3",
          """at input 'location'"""
        ),
        (
          // perr: latitude out of spec
          """
            |2017-01-01
            | # location: geo:123,0
            | e 1
            | a
            |
            |""".stripMargin,
          "on line: 3",
          """at input 'location'"""
        ),
        (
          // perr: longitude out of spec
          """
            |2017-01-01
            | # location: geo:0,1234
            | e 1
            | a
            |
            |""".stripMargin,
          "on line: 3",
          """at input ' '"""
        ),

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
}