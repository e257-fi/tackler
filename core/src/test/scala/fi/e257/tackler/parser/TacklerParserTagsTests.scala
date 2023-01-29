/*
 * Copyright 2020-2023 E257.FI
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

import fi.e257.tackler.api.TxnHeader
import fi.e257.tackler.core.{Settings, TacklerException}
import org.scalatest.funspec.AnyFunSpec

class TacklerParserTagsTests extends AnyFunSpec {

  describe("Metadata (Tags)") {

    val spaces = " \t   \t"
    /**
      * test: 4d364251-f578-4c00-8390-9d8b5feea90b
      */
    it("rejects invalid tags metadata constructions") {
      val perrStrings: List[(String, String, String)] = List(
        (
          """
            |2020-12-24
            | # tags: ,tuv
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 3",
          """at input ','"""
        ),
        (
          """
            |2020-12-24
            | # tags: tuv,
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 3",
          """no viable alternative at input"""
        ),
        (
          """
            |2020-12-24
            | # tags: tuv,,
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 3",
          """at input ','"""
        ),
        (
          """
            |2020-12-24
            | # tags: tuv, ,
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 3",
          """at input ','"""
        ),
        (
          """
            |2020-12-24
            | # tags: tu v
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 3",
          """at input ' '"""
        ),
        (
          """
            |2020-12-24
            | # tags: :tuv
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 3",
          """at input ':'"""
        ),
        (
          """
            |2020-12-24
            | # tags: tuv:
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 3",
          """at input 'tuv'"""
        ),
        (
          """
            |2020-12-24
            | # tags: tu::v
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 3",
          """at input 'tu'"""
        ),

        (
          """
            |2020-12-24
            | ; metadata must be first
            | # tags: t,us
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 4",
          """at input ' #'"""
        ),
        (
          """
            |2020-12-24
            | # tags: t,u,v
            | ; no comments between metadata
            | # uuid: ff692918-290e-4b45-b78e-dba45619eec2
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 5",
          """at input ' #'"""
        ),
        (
          """
            |2020-12-24
            | # tags: t,u
            | # tags: v,x
            | a 1
            | e 1
            |
            |""".stripMargin,
          "on line: 4",
          """at input ' """
        ),
        (
          """
            |2020-12-24
            | # location: geo:60,25
            | # tags: tuv
            | # location: geo:61,25
            | a  1
            | e -1
            |
            |""".stripMargin,
          "on line: 5",
          """at input ' """
        ),
        (
          """
            |2020-12-24
            | # tags: t,u
            | # location: geo:60,25
            | # tags: x,y
            | a  1
            | e -1
            |
            |""".stripMargin,
          "on line: 5",
          """at input ' """
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

      assert(count === perrStrings.length)
    }

    /**
     * test: 32e2d33d-f357-4751-8286-605cee07ea78
     */
    it("reject duplicate tags in txn tags set") {
      val tt = new TacklerTxns(Settings())

      val perrStrings: List[(String, String)] = List(
        (
          """
            |2023-01-29
            | # tags: a, b, c, a
            | a 1
            | e 1
            |
            |""".stripMargin,
          "duplicate",
        ),
      )
      val count = perrStrings.map(perrStr => {
        val ex = intercept[TacklerException]({
          val _ = tt.string2Txns(perrStr._1)
        })
        assert(ex.getMessage.contains(perrStr._2))
        1
      }).foldLeft(0)(_ + _)

      assert(count === perrStrings.length)
    }

     /**
      * test: df593f17-2c74-4657-8da9-afc9ba445755
      */
    it("accepts tags metadata") {
      val pokStrings: List[
        (String, Int,
          List[(String, (TxnHeader => String))])] = List(
        (
          """
            |2020-12-24
            | # location: geo:61,25
            | # uuid: 369d63de-7a3b-4a3f-a741-a592fad19b9f
            | # tags: a:b:c
            | a  1
            | e -1
            |
            |""".stripMargin,
          3, List(
          ("369d63de-7a3b-4a3f-a741-a592fad19b9f", { hdr: TxnHeader => hdr.uuid.map(_.toString).getOrElse("barf") }),
          ("geo:61,25", { hdr: TxnHeader => hdr.location.map(_.toString).getOrElse("barf") }),
          ("a:b:c", { hdr: TxnHeader => hdr.tags.map(_.mkString("", ",", "")).getOrElse("barf") }))
        ),
        (
          """
            |2020-12-24
            | # tags: a
            | a  1
            | e -1
            |
            |""".stripMargin,
          1, List(
          ("a", { hdr: TxnHeader => hdr.tags.map(_.mkString("", ", ", "")).getOrElse("barf") }))
        ),
        (
          """
            |2020-12-24
            | # tags: a, b
            | a  1
            | e -1
            |
            |""".stripMargin,
          1, List(
          ("a, b", { hdr: TxnHeader => hdr.tags.map(_.mkString("", ", ", "")).getOrElse("barf") }))
        ),
        (
          """
            |2020-12-24
            | # tags: a, b, c
            | a  1
            | e -1
            |
            |""".stripMargin,
          1, List(
          ("a, b, c", { hdr: TxnHeader => hdr.tags.map(_.mkString("", ", ", "")).getOrElse("barf") }))
        ),
        (
          """
            |2020-12-24
            | # tags: a, b, c, d
            | a  1
            | e -1
            |
            |""".stripMargin,
          1, List(
          ("a, b, c, d", { hdr: TxnHeader => hdr.tags.map(_.mkString("", ", ", "")).getOrElse("barf") }))
        ),
        (
          """
            |2020-12-24
            | # tags: a, b, c, d, e
            | a  1
            | e -1
            |
            |""".stripMargin,
          1, List(
          ("a, b, c, d, e", { hdr: TxnHeader => hdr.tags.map(_.mkString("", ", ", "")).getOrElse("barf") }))
        ),
        (
          """
            |2020-12-24
            | # tags: e, c, a:b, b, d
            | a  1
            | e -1
            |
            |""".stripMargin,
          1, List(
          ("e, c, a:b, b, d", { hdr: TxnHeader => hdr.tags.map(_.mkString("", ", ", "")).getOrElse("barf") }))
        ),
        (
          """
            |2020-12-24
            | # tags: a:b:c, d, e
            | a  1
            | e -1
            |
            |""".stripMargin,
          1, List(
          ("a:b:c, d, e", { hdr: TxnHeader => hdr.tags.map(_.mkString("", ", ", "")).getOrElse("barf") }))
        ),
        (
          """
            |2020-12-24
            | # tags: a:b:c , d ,e
            | a  1
            | e -1
            |
            |""".stripMargin,
          1, List(
          ("a:b:c, d, e", { hdr: TxnHeader => hdr.tags.map(_.mkString("", ", ", "")).getOrElse("barf") }))
        ),
        (
          s"""
            |2020-12-24
            | #${spaces}tags:${spaces}a:b:c${spaces},${spaces}d${spaces},${spaces}e${spaces}
            | a  1
            | e -1
            |
            |""".stripMargin,
          1, List(
          ("a:b:c, d, e", { hdr: TxnHeader => hdr.tags.map(_.mkString("", ", ", "")).getOrElse("barf") }))
        ),
      )

      val tt = new TacklerTxns(Settings())

      val totalTestCount = pokStrings.map(pokStr => {
        val txnData = tt.string2Txns(pokStr._1)

        val testVector = pokStr._3
        val testCount = testVector.map(test => {
          assert(test._1 === test._2(txnData.txns.head.header))
          1
        }).foldLeft(0)(_ + _)

        assert(testCount === pokStr._2)
        1
      }).foldLeft(0)(_ + _)
      assert(totalTestCount === pokStrings.length)
    }
  }
}
