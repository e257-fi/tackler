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

class TacklerParserHeaderDescriptionTest extends FunSpec {

  describe("Description") {

    /**
     * test: 03d3df34-e68a-4104-b8ab-be06d36bf189
     */
    ignore("check invalid description constructs") {
      val perrStrings: List[(String, String, String)] = List(
        (
          """
            |2017-01-01 (123) abc
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' abc'"""
        ),
        (
          """
            |2017-01-01 (123) (abc
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' ('"""
        ),
        (
          """
            |2017-01-01 )abc
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' )'"""
        ),
        (
          """
            |2017-01-01 +02:00
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' +02'"""
        ),
        (
          """
            |2017-01-01 -02:00
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' -02'"""
        ),
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
            |2017-01-01 T 00:00:00Z
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' T'"""
        ),

        (
          """
            |2017-01-01 T 00:00:00 Z
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' T'"""
        ),

        (
          """
            |2017-01-01 (123) )abc
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' )'"""
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

      assert(count === 9)
    }

    /**
     * test: 58d08778-10ee-489c-bb91-7059b9ba0cca
     */
    it("accept valid description constructs") {
      val pokStrings: List[(String, String)] = List(
        (
          """
            |2017-01-01 'abc
            | a 1
            | e -1
            |
            |""".stripMargin,
          "abc"
        ),
        (
          """
            |2017-01-01   'abc
            | a 1
            | e -1
            |
            |""".stripMargin,
          "abc"
        ),
        (
          s"""
            |2017-01-01 ${"\t \t"}   'abc
            | a 1
            | e -1
            |
            |""".stripMargin,
          "abc"
        ),
        (
          s"""
             |2017-01-01 'abc${"   "}
             | a 1
             | e -1
             |
            |""".stripMargin,
          "abc"
        ),
        (
          s"""
             |2017-01-01 'abc${" \t "}
             | a 1
             | e -1
             |
            |""".stripMargin,
          "abc"
        ),
        (
          """
            |2017-01-01 '123
            | a 1
            | e -1
            |
            |""".stripMargin,
          "123"
        ),
        (
          """
            |2017-01-01 '1.23
            | a 1
            | e -1
            |
            |""".stripMargin,
          "1.23"
        ),
        (
          """
            |2017-01-01 '(abc
            | a 1
            | e -1
            |
            |""".stripMargin,
          "(abc"
        ),
        (
          """
            |2017-01-01   '
            | a 1
            | e -1
            |
            |""".stripMargin,
          ""
        ),
        (
          """
            |2017-01-01  '   a
            | a 1
            | e -1
            |
            |""".stripMargin,
          "   a"
        ),
        (
          """
            |2017-01-01 'abc'
            | a 1
            | e -1
            |
            |""".stripMargin,
          "abc'"
        ),
        (
          """
            |2017-01-01 ''
            | a 1
            | e -1
            |
            |""".stripMargin,
          "'"
        ),
        (
          """
            |2017-01-01  '  '
            | a 1
            | e -1
            |
            |""".stripMargin,
          "  '"
        ),
        (
          s"""
            |2017-01-01  '''
            | a 1
            | e -1
            |
            |""".stripMargin,
          "''"
        ),
        (
          s"""
            |2017-01-01  ''''
            | a 1
            | e -1
            |
            |""".stripMargin,
          "'''"
        ),
        (
          """
            |2017-01-01 'a'b'
            | a 1
            | e -1
            |
            |""".stripMargin,
          "a'b'"
        ),
        (
          """
            |2017-01-01 'a'b''
            | a 1
            | e -1
            |
            |""".stripMargin,
          "a'b''"
        ),
      )

      val tt = new TacklerTxns(Settings())

      val count = pokStrings.map(pokStr => {
        val txnData = tt.string2Txns(pokStr._1)

        assert(txnData.txns.head.header.description.getOrElse("this-will-not-match") === pokStr._2)
        1
      }).foldLeft(0)(_ + _)
      assert(count === 17)
    }


    /**
     * test:
     */
    it("accept valid code + description constructs") {
      val pokStrings: List[(String, String, String)] = List(
        (
          """
            |2017-01-01 (123) 'abc
            | a 1
            | e -1
            |
            |""".stripMargin,
          "123",
          "abc"
        ),
        (
          s"""
            |2017-01-01 (123) ${" \t "}'abc
            | a 1
            | e -1
            |
            |""".stripMargin,
          "123",
          "abc"
        ),
        (
          s"""
             |2017-01-01${" \t "}(123)${" \t "}'abc
             | a 1
             | e -1
             |
            |""".stripMargin,
          "123",
          "abc"
        ),

        (
          s"""
            |2017-01-01 (123) ${" \t "}'(abc
            | a 1
            | e -1
            |
            |""".stripMargin,
          "123",
          "(abc"
        ),
        (
          """
            |2017-01-01 (123) '
            | a 1
            | e -1
            |
            |""".stripMargin,
          "123",
          ""
        ),
        (
          s"""
             |2017-01-01 (123) '${" \t "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "123",
          ""
        ),
        (
          """
            |2017-01-01 (123) '   a
            | a 1
            | e -1
            |
            |""".stripMargin,
          "123",
          "   a"
        ),
        (
          """
            |2017-01-01 (123) 'abc'
            | a 1
            | e -1
            |
            |""".stripMargin,
          "123",
          "abc'"
        ),
        (
          """
            |2017-01-01 (123) ''
            | a 1
            | e -1
            |
            |""".stripMargin,
          "123",
          "'"
        ),
        (
          """
            |2017-01-01 (123) '  '
            | a 1
            | e -1
            |
            |""".stripMargin,
          "123",
          "  '"
        ),
        (
          s"""
            |2017-01-01 (123) '''
            | a 1
            | e -1
            |
            |""".stripMargin,
          "123",
          "''"
        ),
        (
          """
            |2017-01-01 (123) ''''
            | a 1
            | e -1
            |
            |""".stripMargin,
          "123",
          "'''"
        ),
        (
          """
            |2017-01-01 (123) 'a'b'
            | a 1
            | e -1
            |
            |""".stripMargin,
          "123",
          "a'b'"
        ),
        (
          """
            |2017-01-01 (123) 'a'b''
            | a 1
            | e -1
            |
            |""".stripMargin,
          "123",
          "a'b''"
        ),
      )

      val tt = new TacklerTxns(Settings())

      val count = pokStrings.map(pokStr => {
        val txnData = tt.string2Txns(pokStr._1)

        assert(txnData.txns.head.header.code.getOrElse("this-will-not-match") === pokStr._2)
        assert(txnData.txns.head.header.description.getOrElse("this-will-not-match") === pokStr._3)
        1
      }).foldLeft(0)(_ + _)
      assert(count === 14)
    }
  }
}
