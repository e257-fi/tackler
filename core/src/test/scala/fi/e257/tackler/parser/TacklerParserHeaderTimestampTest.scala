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
     * test:
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
        /*
        todo: perr: tz
        (
          """
            |2017-01-01 +02:00
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' +02:00'"""
        ),
         */

        (
          """
            |2017-01-01+0200
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input '+0200'"""
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
        /*
        todo: perr: tz
        (
          """
            |2017-01-01T14:00:00 +02:00
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' +02:00'"""
        ),
        */
        (
          """
            |2017-01-01T14:00:00+0200
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input '+0200'"""
        ),

        /*
        todo: perr: tz
        (
          """
            |2017-01-01 -04:00
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' -04:00'"""
        ),
         */

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
        /*
        todo: perr: tz
        (
          """
            |2017-01-01T14:00:00 -04:00
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' -04:00'"""
        ),
         */

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
     * test:
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
