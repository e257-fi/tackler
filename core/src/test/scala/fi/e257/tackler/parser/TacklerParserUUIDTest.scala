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

class TacklerParserUUIDTest extends FunSpec {

  describe("Metadata (uuid)") {

    /**
     * test: 49f73bec-afd9-4bef-bf5b-f9439ab2ea47
     */
    it("check invalid metadata constructs") {
      val perrStrings: List[(String, String, String)] = List(
        (
          """
            |2017-01-01
            | # uid: 2c01d889-c928-477b-bf53-55e19887d34b
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 3",
          """at input ' # uid'"""
        ),
        (
          """
            |2017-01-01
            | #:uuid: 2c01d889-c928-477b-bf53-55e19887d34b
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 3",
          """at input ' #:'"""
        ),
        (
          """
            |2017-01-01
            | #uuid: 2c01d889-c928-477b-bf53-55e19887d34b
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 3",
          """at input ' #uuid'"""
        ),
        (
          """
            |2017-01-01
            | # uuid:: 2c01d889-c928-477b-bf53-55e19887d34b
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 3",
          """at input ' # uuid::'"""
        ),
        (
          """
            |2017-01-01
            | # uuid 2c01d889-c928-477b-bf53-55e19887d34b
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 3",
          """at input ' # uuid '"""
        ),
        (
          """
            |2017-01-01
            | ;:uuid: 688fca6a-86e2-4c9d-82a0-1384a386167f
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 3",
          """at input ';'"""
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
     * test: 546e4368-dcfa-44d5-a21d-13f3b8bf51b6
     */
    it("accept valid metadata constructs") {
      val pokStrings: List[(String, String)] = List(

        (
          """
            |2017-01-01
            | # uuid: 0e3f2e08-1789-47ed-b93b-1280994586ac
            | a  1
            | e -1
            |
            |""".stripMargin,
          "0e3f2e08-1789-47ed-b93b-1280994586ac"
        ),
        (
          s"""
             |2017-01-01
             | #      uuid:     52c319c4-fb42-4a81-bdce-95979b602ba0
             | a  1
             | e -1
             |
             |""".stripMargin,
          "52c319c4-fb42-4a81-bdce-95979b602ba0"
        ),
        (
          s"""
             |2017-01-01${"\t"}
             | #${"\t\t"}uuid:${"\t\t"}3e75fa97-4be9-4955-acb9-6349223d4cbc
             | a  1
             | e -1
             |
             |""".stripMargin,
          "3e75fa97-4be9-4955-acb9-6349223d4cbc"
        ),
        (
          s"""
             |2017-01-01
             | #${"\t \t"}uuid:${"\t \t "}fec05984-b8a6-439d-8bb0-0ac6461fba8e
             | a  1
             | e -1
             |
             |""".stripMargin,
          "fec05984-b8a6-439d-8bb0-0ac6461fba8e"
        ),
        (
          s"""
             |2017-01-01
             | #${"\t \t"}uuid:${"\t \t "}4c5bab64-edf9-4972-bce6-09cdd666f89d${"\t \t "}
             | a  1
             | e -1
             |
             |""".stripMargin,
          "4c5bab64-edf9-4972-bce6-09cdd666f89d"
        ),
      )

      val tt = new TacklerTxns(Settings())

      val count = pokStrings.map(pokStr => {
        val txnData = tt.string2Txns(pokStr._1)

        assert(txnData.txns.head.header.uuid.map(_.toString).getOrElse("this-will-not-match") === pokStr._2)
        1
      }).foldLeft(0)(_ + _)
      assert(count === 5)
    }
  }
}
