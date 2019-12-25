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

class TacklerParserHeaderCodeTest extends AnyFunSpec {

  describe("Code") {

    /**
     * test: 242aa119-bc5e-4562-9f4a-5feb26d1fba6
     */
    it("check invalid code constructs") {
      val perrStrings: List[(String, String, String)] = List(
        (
          """
            |2017-01-01 (123
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' '"""
        ),
        (
          """
            |2017-01-01 (123))
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ')'"""
        ),
        (
          """
            |2017-01-01 ((123))
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' '"""
        ),
        (
          """
            |2017-01-01 (123)abc
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input 'abc'"""
        ),
        (
          """
            |2017-01-01 (123)a)
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input 'a'"""
        ),
        (
          """
            |2017-01-01 (a'a)
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' '"""
        ),
        (
          """
            |2017-01-01 (a[a)
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' '"""
        ),
        (
          """
            |2017-01-01 (a]a)
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' '"""
        ),
        (
          """
            |2017-01-01 (a{a)
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' '"""
        ),
        (
          """
            |2017-01-01 (a}a)
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' '"""
        ),
        (
          """
            |2017-01-01 (a<a)
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' '"""
        ),
        (
          """
            |2017-01-01 (a>a)
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' '"""
        ),

        (
          """
            |2017-01-01 ( ' )
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' '"""
        ),
        (
          """
            |2017-01-01 ( [ )
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' '"""
        ),
        (
          """
            |2017-01-01 ( ] )
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' '"""
        ),
        (
          """
            |2017-01-01 ( { )
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' '"""
        ),
        (
          """
            |2017-01-01 ( } )
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' '"""
        ),
        (
          """
            |2017-01-01 ( < )
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' '"""
        ),
        (
          """
            |2017-01-01 ( > )
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' '"""
        ),


        (
          """
            |2017-01-01 ( [a] )
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' '"""
        ),
        (
          """
            |2017-01-01 ( {a} )
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
          """at input ' '"""
        ),
        (
          """
            |2017-01-01 ( <a> )
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 2",
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

      assert(count === 22)
    }

    /**
     * test: a5450ec6-42a3-4f3b-b989-27eb2949ccad
     */
    it("accept valid code constructs") {
      val pokStrings: List[(String, String)] = List(

        (
          """
            |2017-01-01 (abc)
            | a 1
            | e -1
            |
            |""".stripMargin,
          "abc"
        ),
        (
          """
            |2017-01-01  (abc)
            | a 1
            | e -1
            |
            |""".stripMargin,
          "abc"
        ),
        (
          s"""
             |2017-01-01${"\t"}(abc)
             | a 1
             | e -1
             |
             |""".stripMargin,
          "abc"
        ),
        (
          s"""
             |2017-01-01${"\t \t "}(abc)
             | a 1
             | e -1
             |
             |""".stripMargin,
          "abc"
        ),
        (
          s"""
             |2017-01-01 (abc)${"  "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "abc"
        ),
        (
          s"""
             |2017-01-01 (abc)${"\t \t "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "abc"
        ),
        (
          """
            |2017-01-01 (a c)
            | a 1
            | e -1
            |
            |""".stripMargin,
          "a c"
        ),
        (
          """
            |2017-01-01 ()
            | a 1
            | e -1
            |
            |""".stripMargin,
          ""
        ),
        (
          s"""
             |2017-01-01 (${"\t \t "})
             | a 1
             | e -1
             |
             |""".stripMargin,
          ""
        ),
        (
          """
            |2017-01-01 ( )
            | a 1
            | e -1
            |
            |""".stripMargin,
          ""
        ),
        (
          """
            |2017-01-01 (!)
            | a 1
            | e -1
            |
            |""".stripMargin,
          "!"
        ),
        (
          """
            |2017-01-01 (*)
            | a 1
            | e -1
            |
            |""".stripMargin,
          "*"
        ),
        (
          s"""
             |2017-01-01 ${"\t \t"}   (123)
             | a 1
             | e -1
             |
             |""".stripMargin,
          "123"
        ),
        (
          s"""
             |2017-01-01 ${"\t \t"}   (123) ${"\t \t "}
             | a 1
             | e -1
             |
             |""".stripMargin,
          "123"
        ),
        (
          """
            |2017-01-01 (abc)
            | a 1
            | e -1
            |
            |""".stripMargin,
          "abc"
        ),
        (
          s"""
             |2017-01-01 (${"\t \t"}123)
             | a 1
             | e -1
             |
             |""".stripMargin,
          "123"
        ),
        (
          s"""
             |2017-01-01 (123${"\t \t "})
             | a 1
             | e -1
             |
             |""".stripMargin,
          "123"
        ),
        (
          s"""
             |2017-01-01 (${"\t \t"}123)
             | a 1
             | e -1
             |
             |""".stripMargin,
          "123"
        ),
        (
          s"""
             |2017-01-01 (${"\t \t "}123${"\t \t "})
             | a 1
             | e -1
             |
             |""".stripMargin,
          "123"
        ),

      )

      val tt = new TacklerTxns(Settings())

      val count = pokStrings.map(pokStr => {
        val txnData = tt.string2Txns(pokStr._1)

        assert(txnData.txns.head.header.code.getOrElse("this-will-not-match") === pokStr._2)
        1
      }).foldLeft(0)(_ + _)
      assert(count === 19)
    }
  }
}
