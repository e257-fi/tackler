/*
 * Copyright 2023 E257.FI
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

package fi.e257.tackler.api

import org.scalatest.funspec.AnyFunSpecLike
import io.circe
import io.circe.parser.decode
import io.circe.syntax._

import java.time.ZonedDateTime
import java.util.UUID

class TxnFilterHeaderTest extends AnyFunSpecLike {

  def getFilter(json: Either[circe.Error, TxnFilterDefinition]): TxnFilterDefinition = {
    json.toOption.get
  }

  val txnFilterFalse = TxnFilterNone()
  val txnFilterTrue = TxnFilterAll()

  describe("TxnTSBegin") {
    /**
     * test: baa0038e-45b7-4911-a647-859de2da4716
     * desc: TxnTSBegin, JSON
     */
    it("JSON") {
      val filterJsonStr =
        """{"txnFilter":{"TxnFilterTxnTSBegin":{"begin":"2023-02-25T10:11:22.345+02:00"}}}"""

      val filterTextStr =
        """Filter:
          |  Txn TS: begin 2023-02-25T10:11:22.345+02:00
          |""".stripMargin

      val txnFilterRoot: Either[circe.Error, TxnFilterDefinition] = decode[TxnFilterDefinition](filterJsonStr)

      val tf = getFilter(txnFilterRoot)

      assert(filterTextStr === tf.text("").mkString("", "\n", "\n"))
      assert(filterJsonStr === tf.asJson.noSpaces)
    }

    /**
     * test: c01de4f4-0e07-4d8d-a4c8-2d1ad28df264
     * desc: TxnTSBegin, Text
     */
    it("Text") {
      val filterTextStr =
        """>Filter:
          |>  AND
          |>    Txn TS: begin 2023-02-25T10:11:22.345+02:00
          |>    AND
          |>      Txn TS: begin 2023-02-25T20:11:22.345+02:00
          |>      All pass
          |""".stripMargin

      val tfd = TxnFilterDefinition(
        TxnFilterAND(List[TxnFilter](
          TxnFilterTxnTSBegin(ZonedDateTime.parse("2023-02-25T10:11:22.345+02:00")),
          TxnFilterAND(List[TxnFilter](
            TxnFilterTxnTSBegin(ZonedDateTime.parse("2023-02-25T20:11:22.345+02:00")),
            txnFilterTrue
          )),
        )),
      )

      val text = tfd.text(">").mkString("", "\n", "\n")
      assert(filterTextStr === text)
    }
  }

  describe("TxnTSEnd") {
    /**
     * test: db171b86-7435-4e9b-bfa0-4288c720289c
     * desc: TxnTSEnd, JSON
     */
    it("JSON") {
      val filterJsonStr =
        """{"txnFilter":{"TxnFilterTxnTSEnd":{"end":"2023-02-25T10:11:22.345+02:00"}}}"""

      val filterTextStr =
        """Filter:
          |  Txn TS: end   2023-02-25T10:11:22.345+02:00
          |""".stripMargin

      val txnFilterRoot: Either[circe.Error, TxnFilterDefinition] = decode[TxnFilterDefinition](filterJsonStr)

      val tf = getFilter(txnFilterRoot)

      assert(filterTextStr === tf.text("").mkString("", "\n", "\n"))
      assert(filterJsonStr === tf.asJson.noSpaces)
    }

    /**
     * test: ef2348e6-3684-4a13-85e9-5aec89a9e3bb
     * desc: TxnTSEnd, Text
     */
    it("Text") {
      val filterTextStr =
        """>Filter:
          |>  AND
          |>    Txn TS: end   2023-02-25T10:11:22.345+02:00
          |>    AND
          |>      Txn TS: end   2023-02-25T20:11:22.345+02:00
          |>      All pass
          |""".stripMargin

      val tfd = TxnFilterDefinition(
        TxnFilterAND(List[TxnFilter](
          TxnFilterTxnTSEnd(ZonedDateTime.parse("2023-02-25T10:11:22.345+02:00")),
          TxnFilterAND(List[TxnFilter](
            TxnFilterTxnTSEnd(ZonedDateTime.parse("2023-02-25T20:11:22.345+02:00")),
            txnFilterTrue
          )),
        )),
      )

      val text = tfd.text(">").mkString("", "\n", "\n")
      assert(filterTextStr === text)
    }
  }

  describe("TxnCode") {
    /**
     * test: 928a78b4-0ad7-4909-b145-3826acc75b3d
     * desc: TxnCode, JSON
     */
    it("JSON") {
      val filterJsonStr =
        """{"txnFilter":{"TxnFilterTxnCode":{"regex":"(abc.*)|(def.*)"}}}"""

      val filterTextStr =
        """Filter:
          |  Txn Code: "(abc.*)|(def.*)"
          |""".stripMargin

      val txnFilterRoot: Either[circe.Error, TxnFilterDefinition] = decode[TxnFilterDefinition](filterJsonStr)

      val tf = getFilter(txnFilterRoot)

      assert(filterTextStr === tf.text("").mkString("", "\n", "\n"))
      assert(filterJsonStr === tf.asJson.noSpaces)
    }

    /**
     * test: 274ccbb4-dcd7-431d-bf05-5da1b191d74c
     * desc: TxnCode, Text
     */
    it("Text") {
      val filterTextStr =
        """>Filter:
          |>  AND
          |>    Txn Code: "(abc.*)|(def.*)"
          |>    AND
          |>      Txn Code: "xyz"
          |>      All pass
          |""".stripMargin

      val tfd = TxnFilterDefinition(
        TxnFilterAND(List[TxnFilter](
          TxnFilterTxnCode("(abc.*)|(def.*)"),
          TxnFilterAND(List[TxnFilter](
            TxnFilterTxnCode("xyz"),
            txnFilterTrue
          )),
        )),
      )

      val text = tfd.text(">").mkString("", "\n", "\n")
      assert(filterTextStr === text)
    }
  }

  describe("TxnDescription") {
    /**
     * test: 9cb8321a-0c43-4a24-b21e-0286dbe503cd
     * desc: TxnDescription, JSON
     */
    it("JSON") {
      val filterJsonStr =
        """{"txnFilter":{"TxnFilterTxnDescription":{"regex":"(abc.*)|(def.*)"}}}"""

      val filterTextStr =
        """Filter:
          |  Txn Description: "(abc.*)|(def.*)"
          |""".stripMargin

      val txnFilterRoot: Either[circe.Error, TxnFilterDefinition] = decode[TxnFilterDefinition](filterJsonStr)

      val tf = getFilter(txnFilterRoot)

      assert(filterTextStr === tf.text("").mkString("", "\n", "\n"))
      assert(filterJsonStr === tf.asJson.noSpaces)
    }

    /**
     * test: ea88d0cf-2c60-45ac-835d-6f2f18a2c10d
     * desc: TxnDescription, Text
     */
    it("Text") {
      val filterTextStr =
        """>Filter:
          |>  AND
          |>    Txn Description: "(abc.*)|(def.*)"
          |>    AND
          |>      Txn Description: "xyz"
          |>      All pass
          |""".stripMargin

      val tfd = TxnFilterDefinition(
        TxnFilterAND(List[TxnFilter](
          TxnFilterTxnDescription("(abc.*)|(def.*)"),
          TxnFilterAND(List[TxnFilter](
            TxnFilterTxnDescription("xyz"),
            txnFilterTrue
          )),
        )),
      )

      val text = tfd.text(">").mkString("", "\n", "\n")
      assert(filterTextStr === text)
    }
  }

  describe("TxnUUID") {
    /**
     * test: 9ad41df9-c153-458b-a941-3b4763c25548
     * desc: TxnUUID, JSON
     */
    it("JSON") {
      val filterJsonStr =
        """{"txnFilter":{"TxnFilterTxnUUID":{"uuid":"8c913372-48e9-466c-a897-11b151548a19"}}}"""

      val filterTextStr =
        """Filter:
          |  Txn UUID: 8c913372-48e9-466c-a897-11b151548a19
          |""".stripMargin

      val txnFilterRoot: Either[circe.Error, TxnFilterDefinition] = decode[TxnFilterDefinition](filterJsonStr)

      val tf = getFilter(txnFilterRoot)

      assert(filterTextStr === tf.text("").mkString("", "\n", "\n"))
      assert(filterJsonStr === tf.asJson.noSpaces)
    }

    /**
     * test: e388aecd-8500-4f89-98c6-9588199c104f
     * desc: TxnUUID, Text
     */
    it("Text") {
      val filterTextStr =
        """>Filter:
          |>  AND
          |>    Txn UUID: 76a0f143-d64e-4497-b357-5ae2eb092219
          |>    AND
          |>      Txn UUID: f01df5b5-18e2-477c-aaac-3e0b672b2729
          |>      All pass
          |""".stripMargin

      val tfd = TxnFilterDefinition(
        TxnFilterAND(List[TxnFilter](
          TxnFilterTxnUUID(UUID.fromString("76a0f143-d64e-4497-b357-5ae2eb092219")),
          TxnFilterAND(List[TxnFilter](
            TxnFilterTxnUUID(UUID.fromString("f01df5b5-18e2-477c-aaac-3e0b672b2729")),
            txnFilterTrue
          )),
        )),
      )

      val text = tfd.text(">").mkString("", "\n", "\n")
      assert(filterTextStr === text)
    }
  }

  describe("BBoxLatLon") {
    /**
     * test: 05bfe9c0-0dc1-462a-b452-39c2eaf55d02
     * desc: BBoxLatLon, JSON
     */
    it("JSON") {
      val filterJsonStr =
        """{"txnFilter":{"TxnFilterBBoxLatLon":{"south":59.85,"west":24,"north":60.8,"east":27.5}}}"""

      val filterTextStr =
        """Filter:
          |  Txn Bounding Box 2D
          |    North, East: geo:60.8,27.5
          |    South, West: geo:59.85,24
          |""".stripMargin

      val txnFilterRoot: Either[circe.Error, TxnFilterDefinition] = decode[TxnFilterDefinition](filterJsonStr)

      val tf = getFilter(txnFilterRoot)

      assert(filterTextStr === tf.text("").mkString("", "\n", "\n"))
      assert(filterJsonStr === tf.asJson.noSpaces)
    }

    /**
     * test: 89d31f9c-029f-47ce-acb9-ddfaaa089782
     * desc: BBoxLatLon, Text
     */
    it("Text") {
      val filterTextStr =
        """>Filter:
          |>  AND
          |>    Txn Bounding Box 2D
          |>      North, East: geo:60.8,27.5
          |>      South, West: geo:59.85,24
          |>    AND
          |>      Txn Bounding Box 2D
          |>        North, East: geo:60.8,27.5
          |>        South, West: geo:59.85,24
          |>      All pass
          |""".stripMargin

      val tfd = TxnFilterDefinition(
        TxnFilterAND(List[TxnFilter](
          TxnFilterBBoxLatLon(59.85, 24, 60.8, 27.5),
          TxnFilterAND(List[TxnFilter](
            TxnFilterBBoxLatLon(59.85, 24, 60.8, 27.5),
            txnFilterTrue
          )),
        )),
      )

      val text = tfd.text(">").mkString("", "\n", "\n")
      assert(filterTextStr === text)
    }
  }

  describe("BBoxLatLonAlt") {
    /**
     * test: c027ef27-3287-411f-aad9-8185f1b55380
     * desc: BBoxLatLonAlt, JSON
     */
    it("JSON") {
      val filterJsonStr =
        """{"txnFilter":{"TxnFilterBBoxLatLonAlt":{"south":-1,"west":-2,"depth":-3,"north":1,"east":2,"height":3}}}"""

      val filterTextStr =
        """Filter:
          |  Txn Bounding Box 3D
          |    North, East, Height: geo:1,2,3
          |    South, West, Depth:  geo:-1,-2,-3
          |""".stripMargin

      val txnFilterRoot: Either[circe.Error, TxnFilterDefinition] = decode[TxnFilterDefinition](filterJsonStr)

      val tf = getFilter(txnFilterRoot)

      assert(filterTextStr === tf.text("").mkString("", "\n", "\n"))
      assert(filterJsonStr === tf.asJson.noSpaces)
    }

    /**
     * test: 54528f53-49fc-43cf-b3a2-221e02e87bcc
     * desc: BBoxLatLonAlt, Text
     */
    it("Text") {
      val filterTextStr =
        """>Filter:
          |>  AND
          |>    Txn Bounding Box 3D
          |>      North, East, Height: geo:1,2,3
          |>      South, West, Depth:  geo:-1,-2,-3
          |>    AND
          |>      Txn Bounding Box 3D
          |>        North, East, Height: geo:1,2,3
          |>        South, West, Depth:  geo:-1,-2,-3
          |>      All pass
          |""".stripMargin

      val tfd = TxnFilterDefinition(
        TxnFilterAND(List[TxnFilter](
          TxnFilterBBoxLatLonAlt(-1, -2, -3, 1, 2, 3),
          TxnFilterAND(List[TxnFilter](
            TxnFilterBBoxLatLonAlt(-1, -2, -3, 1, 2, 3),
            txnFilterTrue
          )),
        )),
      )

      val text = tfd.text(">").mkString("", "\n", "\n")
      assert(filterTextStr === text)
    }
  }

  describe("TxnTags") {
    /**
     * test: 38c85ae0-8c60-4533-946d-c80b788dc262
     * desc: TxnTags, JSON
     */
    it("JSON") {
      val filterJsonStr =
        """{"txnFilter":{"TxnFilterTxnTags":{"regex":"(abc.*)|(def.*)"}}}"""

      val filterTextStr =
        """Filter:
          |  Txn Tags: "(abc.*)|(def.*)"
          |""".stripMargin

      val txnFilterRoot: Either[circe.Error, TxnFilterDefinition] = decode[TxnFilterDefinition](filterJsonStr)

      val tf = getFilter(txnFilterRoot)

      assert(filterTextStr === tf.text("").mkString("", "\n", "\n"))
      assert(filterJsonStr === tf.asJson.noSpaces)
    }

    /**
     * test: 423ccf5f-4dc7-49fb-a972-5a9c09717140
     * desc: TxnTags, Text
     */
    it("Text") {
      val filterTextStr =
        """>Filter:
          |>  AND
          |>    Txn Tags: "(abc.*)|(def.*)"
          |>    AND
          |>      Txn Tags: "xyz"
          |>      All pass
          |""".stripMargin

      val tfd = TxnFilterDefinition(
        TxnFilterAND(List[TxnFilter](
          TxnFilterTxnTags("(abc.*)|(def.*)"),
          TxnFilterAND(List[TxnFilter](
            TxnFilterTxnTags("xyz"),
            txnFilterTrue
          )),
        )),
      )

      val text = tfd.text(">").mkString("", "\n", "\n")
      assert(filterTextStr === text)
    }
  }

  describe("TxnComments") {
    /**
     * test: de0054ff-92e2-4837-b223-40cbbeaa90de
     * desc: TxnComments, JSON
     */
    it("JSON") {
      val filterJsonStr =
        """{"txnFilter":{"TxnFilterTxnComments":{"regex":"(abc.*)|(def.*)"}}}"""

      val filterTextStr =
        """Filter:
          |  Txn Comments: "(abc.*)|(def.*)"
          |""".stripMargin

      val txnFilterRoot: Either[circe.Error, TxnFilterDefinition] = decode[TxnFilterDefinition](filterJsonStr)

      val tf = getFilter(txnFilterRoot)

      assert(filterTextStr === tf.text("").mkString("", "\n", "\n"))
      assert(filterJsonStr === tf.asJson.noSpaces)
    }

    /**
     * test: 5f08fe58-4451-4659-a684-d9725259ce2d
     * desc: TxnComments, Text
     */
    it("Text") {
      val filterTextStr =
        """>Filter:
          |>  AND
          |>    Txn Comments: "(abc.*)|(def.*)"
          |>    AND
          |>      Txn Comments: "xyz"
          |>      All pass
          |""".stripMargin

      val tfd = TxnFilterDefinition(
        TxnFilterAND(List[TxnFilter](
          TxnFilterTxnComments("(abc.*)|(def.*)"),
          TxnFilterAND(List[TxnFilter](
            TxnFilterTxnComments("xyz"),
            txnFilterTrue
          )),
        )),
      )

      val text = tfd.text(">").mkString("", "\n", "\n")
      assert(filterTextStr === text)
    }
  }
}
