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

class TacklerParserMetadataTests extends FunSpec {

  describe("Metadata") {

    /**
      * test: b88d6733-2acf-4021-a3d7-deaf58b518a6
      */
    it("rejects invalid metadata constructions") {
      val perrStrings: List[(String, String, String)] = List(
        (
          """
            |2019-05-01
            | ; metadata must be first
            | # uuid: 2c01d889-c928-477b-bf53-55e19887d34b
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 4",
          """at input ' #'"""
        ),
        (
          """
            |2019-05-01
            | # location: geo:60,25
            | ; no comments between metadata
            | # uuid: f0cf7f01-4af9-41b9-82ae-9601d9e05186
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 5",
          """at input ' #'"""
        ),
        (
          """
            |2019-05-01
            | # uuid: ff46c6d0-c42f-4dfd-a176-beabe95d84a2
            | # uuid: e1bbad16-05ef-4366-8adc-8717b3bb5f38
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 4",
          """at input ' '"""
        ),
        (
          """
            |2019-05-01
            | # location: geo:60,25
            | # location: geo:61,25
            | a  1
            | e -1
            |
            |""".stripMargin,
          "on line: 4",
          """at input ' '"""
        ),
        (
          """
            |2019-05-01
            | # location: geo:60,25
            | # uuid: ea23a28b-a99e-4af4-8f87-c011d606efd7
            | # location: geo:61,25
            | a  1
            | e -1
            |
            |""".stripMargin,
          "on line: 5",
          """at input ' #'"""
        ),
        (
          """
            |2019-05-01
            | # uuid: 5e6ab503-b85b-48ba-bc49-8ed0db2a2ce1
            | # location: geo:60,25
            | # uuid: 552ac798-5807-4875-b64a-e63d02c255d0
            | a  1
            | e -1
            |
            |""".stripMargin,
          "on line: 5",
          """at input ' #'"""
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

    /**
      * test: 5bb95c2e-2fad-4584-9380-e6cafe732cf6
      */
    it("accepts multiple metadata items") {
      val pokStrings: List[(String, String, String)] = List(
        (
          """
            |2019-05-01
            | # uuid: 68ddc754-40ad-4d73-824c-17e75e59c731
            | # location: geo:60,25
            | a  1
            | e -1
            |
            |""".stripMargin,
          "68ddc754-40ad-4d73-824c-17e75e59c731",
          "geo:60,25"
        ),
        (
          """
            |2019-05-01
            | # location: geo:61,25
            | # uuid: c075a1a4-37d5-4d79-a92b-5cbb323519f0
            | a  1
            | e -1
            |
            |""".stripMargin,
          "c075a1a4-37d5-4d79-a92b-5cbb323519f0",
          "geo:61,25"
        ),
      )

      val tt = new TacklerTxns(Settings())

      val count = pokStrings.map(pokStr => {
        val txnData = tt.string2Txns(pokStr._1)

        assert(txnData.txns.head.header.uuid.map(_.toString).getOrElse("this-will-not-match") === pokStr._2)
        assert(txnData.txns.head.header.location.map(_.toString).getOrElse("this-will-not-match") === pokStr._3)
        1
      }).foldLeft(0)(_ + _)
      assert(count === 2)
    }
  }
}