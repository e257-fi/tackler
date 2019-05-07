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

import fi.e257.tackler.api.TxnTS
import fi.e257.tackler.core.Settings
import org.scalatest.FunSpec

class TacklerParserHeaderTimestampTest extends FunSpec {

  describe("Timestamp") {

    /**
     * test: 4ff959f7-c2bd-4750-8664-f46ce50a7c7b
     */
    it("check invalid timestamp constructs") {
      val perrStrings: List[(String, String, String)] = List(
        (
          """
            |2017
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input '2017'"""
        ),
        (
          """
            |2017-1
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input '2017-1'"""
        ),
        (
          """
            |2017-01
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input '2017-01'"""
        ),
        (
          """
            |2017-1-1
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input '2017-1-1'"""
        ),
        (
          """
            |2017-01-1
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input '2017-01-1'"""
        ),
        (
          """
            |2017-01-01+0200
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input '+'"""
        ),
        (
          """
            |2017-01-01T14+02:00
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input '2017-01-01T14'"""
        ),
        (
          """
            |2017-01-01T14:00+02:00
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input '2017-01-01T14'"""
        ),
        (
          """
            |2017-01-01T14:00:00+0200
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input '+'"""
        ),
        (
          """
            |2017-01-01-04:00
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input '2017-01-01-04'"""
        ),
        (
          """
            |2017-01-01T14-04:00
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input '2017-01-01T14-04'"""
        ),
        (
          """
            |2017-01-01T14:00-04:00
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input '2017-01-01T14'"""
        ),
        (
          """
            |2017-01-01T14:00:00-0400
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input '-0400'"""
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

      assert(count === 13)
    }

    /**
     * test: 294a4d37-2911-4c0f-9024-0c79bf3c99ba
     */
    it("check invalid timestamp constructs with format v2") {
      val perrStrings: List[(String, String, String)] = List(
        (
          """
            |2017-01-01 Z
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' Z'"""
        ),
        (
          """
            |2017-01-01 +02:00
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' +'"""
        ),
        (
          """
            |2017-01-01 -04:00
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' -04'"""
        ),
        (
          """
            |2017-01-01T14:00:00 Z
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' Z'"""
        ),
        (
          """
            |2017-01-01T14:00:00 +02:00
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' +'"""
        ),
        (
          """
            |2017-01-01T14:00:00 -04:00
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' -04'"""
        ),
        (
          """
            |2017-01-01 T 14:00:00+02:00
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' T'"""
        ),
        (
          """
            |2017-01-01 T 14:00:00 +02:00
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' T'"""
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

      assert(count === 8)
    }

    /**
     * test: 2c0ee1a2-1a23-4427-a6dc-6156abc36272
     */
    it("accept valid timestamp constructs") {
      val pokStrings: List[(String, String)] = List(

        (
          """
            |2017-06-24
            | a 1
            | e -1
            |
            |""".stripMargin,
          "2017-06-24T00:00:00Z"
        ),
        (
          s"""
             |2017-06-24${"   "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "2017-06-24T00:00:00Z"
        ),
        (
          s"""
             |2017-06-24${"\t \t "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "2017-06-24T00:00:00Z"
        ),
        (
          """
            |2017-06-24T14:01:02
            | a 1
            | e -1
            |
            |""".stripMargin,
          "2017-06-24T14:01:02Z"
        ),
        (
          s"""
             |2017-06-24T14:01:02${"   "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "2017-06-24T14:01:02Z"
        ),
        (
          s"""
             |2017-06-24T14:01:02${"\t \t "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "2017-06-24T14:01:02Z"
        ),
        (
          """
            |2017-06-24T14:01:02Z
            | a 1
            | e -1
            |
            |""".stripMargin,
          "2017-06-24T14:01:02Z"
        ),
        (
          s"""
             |2017-06-24T14:01:02Z${"   "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "2017-06-24T14:01:02Z"
        ),
        (
          s"""
             |2017-06-24T14:01:02Z${"\t \t "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "2017-06-24T14:01:02Z"
        ),
        (
          """
            |2017-06-24T14:01:10+02:00
            | a 1
            | e -1
            |
            |""".stripMargin,
          "2017-06-24T14:01:10+02:00"
        ),
        (
          s"""
             |2017-06-24T14:01:10+02:00${"   "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "2017-06-24T14:01:10+02:00"
        ),
        (
          s"""
             |2017-06-24T14:01:10+02:00${"\t \t "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "2017-06-24T14:01:10+02:00"
        ),

        (
          """
            |2017-06-24T14:01:10-04:00
            | a 1
            | e -1
            |
            |""".stripMargin,
          "2017-06-24T14:01:10-04:00"
        ),
        (
          s"""
             |2017-06-24T14:01:10-04:00${"   "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "2017-06-24T14:01:10-04:00"
        ),
        (
          s"""
             |2017-06-24T14:01:10-04:00${"\t \t "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "2017-06-24T14:01:10-04:00"
        ),

        /*
         * NANO SECOND
         */
        (
          """
            |2017-06-24T14:01:02.123456789
            | a 1
            | e -1
            |
            |""".stripMargin,
          "2017-06-24T14:01:02.123456789Z"
        ),
        (
          s"""
             |2017-06-24T14:01:02.123456789${"   "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "2017-06-24T14:01:02.123456789Z"
        ),
        (
          s"""
             |2017-06-24T14:01:02.123456789${"\t \t "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "2017-06-24T14:01:02.123456789Z"
        ),
        (
          """
            |2017-06-24T14:01:02.123456789Z
            | a 1
            | e -1
            |
            |""".stripMargin,
          "2017-06-24T14:01:02.123456789Z"
        ),
        (
          s"""
             |2017-06-24T14:01:02.123456789Z${"   "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "2017-06-24T14:01:02.123456789Z"
        ),
        (
          s"""
             |2017-06-24T14:01:02.123456789Z${"\t \t "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "2017-06-24T14:01:02.123456789Z"
        ),
        (
          """
            |2017-06-24T14:01:10.123456789+02:00
            | a 1
            | e -1
            |
            |""".stripMargin,
          "2017-06-24T14:01:10.123456789+02:00"
        ),
        (
          s"""
             |2017-06-24T14:01:10.123456789+02:00${"   "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "2017-06-24T14:01:10.123456789+02:00"
        ),
        (
          s"""
             |2017-06-24T14:01:10.123456789+02:00${"\t \t "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "2017-06-24T14:01:10.123456789+02:00"
        ),

        (
          """
            |2017-06-24T14:01:10.123456789-04:00
            | a 1
            | e -1
            |
            |""".stripMargin,
          "2017-06-24T14:01:10.123456789-04:00"
        ),
        (
          s"""
             |2017-06-24T14:01:10.123456789-04:00${"   "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "2017-06-24T14:01:10.123456789-04:00"
        ),
        (
          s"""
             |2017-06-24T14:01:10.123456789-04:00${"\t \t "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "2017-06-24T14:01:10.123456789-04:00"
        ),

      )

      val tt = new TacklerTxns(Settings())

      val count = pokStrings.map(pokStr => {
        val txnData = tt.string2Txns(pokStr._1)

        assert(TxnTS.isoZonedTS(txnData.txns.head.header.timestamp) === pokStr._2)
        1
      }).foldLeft(0)(_ + _)
      assert(count === 27)
    }

  }
}
