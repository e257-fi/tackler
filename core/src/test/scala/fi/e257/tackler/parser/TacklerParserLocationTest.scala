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

import fi.e257.tackler.core.{Settings, TacklerException}
import org.scalatest.funspec.AnyFunSpec

class TacklerParserLocationTest extends AnyFunSpec {

  val tt = new TacklerTxns(Settings())

  describe("Geo URI tests") {

    /**
      * test: bc98cc89-d3b2-468d-9508-8d7a55924178
      */
    it("various valid geo uris") {
      val txnStrs = List(
        (
          """
            |2019-04-01
            | # location: geo:60.170833,24.9375
            | e 1
            | a
            |
            |""".stripMargin,
          "geo:60.170833,24.9375",
        ),
        (
          """
            |2019-04-01
            | # location: geo:66.5436,25.84715,160
            | e 1
            | a
            |
            |""".stripMargin,
          "geo:66.5436,25.84715,160",
        ),
        (
          """
            |2019-04-01
            | # location: geo:66.5436,25.84715,160.0
            | e 1
            | a
            |
            |""".stripMargin,
          "geo:66.5436,25.84715,160.0",
        ),
        (
          """
            |2019-04-01
            | # location: geo:59.90735,16.57532,-155
            | e 1
            | a
            |
            |""".stripMargin,
          "geo:59.90735,16.57532,-155",
        ),
        (
          """
            |2019-04-01
            | # location: geo:59.90735,16.57532,-155.0
            | e 1
            | a
            |
            |""".stripMargin,
          "geo:59.90735,16.57532,-155.0",
        ),
        (
          """
            |2019-04-01
            | # location: geo:0,0,0
            | e 1
            | a
            |
            |""".stripMargin,
          "geo:0,0,0",
        ),
        (
          """
            |2019-04-01
            | # location: geo:-90,0,0
            | e 1
            | a
            |
            |""".stripMargin,
          "geo:-90,0,0",
        ),
        (
          """
            |2019-04-01
            | # location: geo:-90,25,0
            | e 1
            | a
            |
            |""".stripMargin,
          "geo:-90,25,0",
        ),
        (
          """
            |2019-04-01
            | # location: geo:90,0,0
            | e 1
            | a
            |
            |""".stripMargin,
          "geo:90,0,0",
        ),
        (
          """
            |2019-04-01
            | # location: geo:90,25,0
            | e 1
            | a
            |
            |""".stripMargin,
          "geo:90,25,0"
        ),
        (
          """
            |2019-04-01
            | # location: geo:66.56,180,0
            | e 1
            | a
            |
            |""".stripMargin,
          "geo:66.56,180,0",
        ),
        (
          """
            |2019-04-01
            | # location: geo:-66.56,-180.0,0
            | e 1
            | a
            |
            |""".stripMargin,
          "geo:-66.56,-180.0,0",
        )
      )

      val count = txnStrs.map(okStr => {
        val txnData = tt.string2Txns(okStr._1)

        assert(txnData.txns.size === 1)

        assert(txnData.txns.head.header.location.map(_.toString).getOrElse("this-will-not-match") === okStr._2)
        1
      }).foldLeft(0)(_ + _)

      assert(count === 12)
    }

    /**
      * test: c8e7cdf6-3b30-476c-84f0-f5a19812cd28
      */
    it("perr: detect invalid geo uris") {
      val perrStrings: List[(String, String, String)] = List(
        (
          // perr: missing geo-uri
          """
            |2019-05-01
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
            |2019-05-01
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
            |2019-05-01
            | # location: geo:0.0,0.0,0,0
            | e 1
            | a
            |
            |""".stripMargin,
          "on line: 3",
          """at input ' """
        ),
        (
          // perr: missing lat/lon
          """
            |2019-05-01
            | # location: geo:0
            | e 1
            | a
            |
            |""".stripMargin,
          "on line: 3",
          """geo:0"""
        ),
      )

      val count = perrStrings.map(errStr => {
        val ex = intercept[TacklerParseException]({
          val _ = TacklerParser.txnsText(errStr._1)
        })

        assert(ex.getMessage.contains(errStr._2))
        assert(ex.getMessage.contains(errStr._3))
        1
      }).foldLeft(0)(_ + _)

      assert(count === 4)
    }

    /**
      * test: fc711c0d-2820-4f72-8b4c-1219ef578363
      */
    it("detect semantically invalid geo uris") {
      val perrStrings: List[(String, String)] = List(
        (
          // latitude out of spec 1/2
          """
            |2019-05-01
            | # location: geo:-90.1,0
            | e 1
            | a
            |
            |""".stripMargin,
          """for Latitude: -90.1"""
        ),
        (
          // latitude out of spec 2/2
          """
            |2019-05-01
            | # location: geo:90.1,0
            | e 1
            | a
            |
            |""".stripMargin,
          """for Latitude: 90.1"""
        ),
        (
          // longitude out of spec 1/2
          """
            |2019-05-01
            | # location: geo:0,-180.1
            | e 1
            | a
            |
            |""".stripMargin,
          """for Longitude: -180.1"""
        ),
        (
          // longitude out of spec 2/2
          """
            |2019-05-01
            | # location: geo:0,180.1
            | e 1
            | a
            |
            |""".stripMargin,
          """for Longitude: 180.1"""
        ),
        (
          // altitude out of spec
          // Jules Verne: Voyage au centre de la Terre
          """
            |2019-05-01
            | # location: geo:64.8,-23.783333,-6378137.1
            | e 1
            | a
            |
            |""".stripMargin,
          """for Altitude: -6378137.1"""
        ),
      )

      val count = perrStrings.map(errStr => {
        val ex = intercept[TacklerException]({
          val _ = tt.string2Txns(errStr._1)
        })

        assert(ex.getMessage.contains(errStr._2))
        1
      }).foldLeft(0)(_ + _)

      assert(count === 5)
    }
  }
}