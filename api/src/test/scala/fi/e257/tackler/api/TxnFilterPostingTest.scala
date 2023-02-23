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


class TxnFilterPostingTest extends AnyFunSpecLike {

  def getFilter(json: Either[circe.Error, TxnFilterDefinition]): TxnFilterDefinition = {
    json.toOption.get
  }

  val txnFilterFalse = TxnFilterNone()
  val txnFilterTrue = TxnFilterAll()


  describe("PostingAccount") {
    /**
     * test: 44d80d6d-b2cf-47a0-a228-bb2ea068f9f5
     * desc: PostingAccount, JSON
     */
    it("JSON") {
      val filterJsonStr =
        """{"txnFilter":{"TxnFilterPostingAccount":{"regex":"(abc.*)|(def.*)"}}}"""

      val filterTextStr =
        """Filter:
          |  Posting Account: "(abc.*)|(def.*)"
          |""".stripMargin

      val txnFilterRoot: Either[circe.Error, TxnFilterDefinition] = decode[TxnFilterDefinition](filterJsonStr)

      val tf = getFilter(txnFilterRoot)

      assert(filterTextStr === tf.text("").mkString("", "\n", "\n"))
      assert(filterJsonStr === tf.asJson.noSpaces)
    }

    /**
     * test: 382e7f39-90e6-44f0-9162-150e2b353cef
     * desc: PostingAccount, Text
     */
    it("Text") {
      val filterTextStr =
        """>Filter:
          |>  AND
          |>    Posting Account: "(abc.*)|(def.*)"
          |>    AND
          |>      Posting Account: "xyz"
          |>      All pass
          |""".stripMargin

      val tfd = TxnFilterDefinition(
        TxnFilterAND(List[TxnFilter](
          TxnFilterPostingAccount("(abc.*)|(def.*)"),
          TxnFilterAND(List[TxnFilter](
            TxnFilterPostingAccount("xyz"),
            txnFilterTrue
          )),
        )),
      )

      val text = tfd.text(">").mkString("", "\n", "\n")
      assert(filterTextStr === text)
    }
  }

  describe("PostingComment") {
    /**
     * test: 55401f74-0054-42ec-ab0b-17d4c9cda0be
     * desc: PostingComment, JSON
     */
    it("JSON") {
      val filterJsonStr =
        """{"txnFilter":{"TxnFilterPostingComment":{"regex":"(abc.*)|(def.*)"}}}"""

      val filterTextStr =
        """Filter:
          |  Posting Comment: "(abc.*)|(def.*)"
          |""".stripMargin

      val txnFilterRoot: Either[circe.Error, TxnFilterDefinition] = decode[TxnFilterDefinition](filterJsonStr)

      val tf = getFilter(txnFilterRoot)

      assert(filterTextStr === tf.text("").mkString("", "\n", "\n"))
      assert(filterJsonStr === tf.asJson.noSpaces)
    }

    /**
     * test: a1b05b26-3cca-4e56-925d-7ae7602f941a
     * desc: PostingComment, Text
     */
    it("Text") {
      val filterTextStr =
        """>Filter:
          |>  AND
          |>    Posting Comment: "(abc.*)|(def.*)"
          |>    AND
          |>      Posting Comment: "xyz"
          |>      All pass
          |""".stripMargin

      val tfd = TxnFilterDefinition(
        TxnFilterAND(List[TxnFilter](
          TxnFilterPostingComment("(abc.*)|(def.*)"),
          TxnFilterAND(List[TxnFilter](
            TxnFilterPostingComment("xyz"),
            txnFilterTrue
          )),
        )),
      )

      val text = tfd.text(">").mkString("", "\n", "\n")
      assert(filterTextStr === text)
    }
  }

  describe("PostingAmountEqual") {
    /**
     * test: b7b4543d-2ffa-488f-b251-af5a7ba7204f
     * desc: PostingAmountEqual, JSON
     */
    it("JSON") {
      val filterJsonStr =
        """{"txnFilter":{"TxnFilterPostingAmountEqual":{"regex":"(abc.*)|(def.*)","amount":1}}}"""

      val filterTextStr =
        """Filter:
          |  Posting Amount
          |    account: "(abc.*)|(def.*)"
          |    amount == 1
          |""".stripMargin

      val txnFilterRoot: Either[circe.Error, TxnFilterDefinition] = decode[TxnFilterDefinition](filterJsonStr)

      val tf = getFilter(txnFilterRoot)

      assert(filterTextStr === tf.text("").mkString("", "\n", "\n"))
      assert(filterJsonStr === tf.asJson.noSpaces)
    }

    /**
     * test: c0f88d70-c388-4c4f-9cca-f29b921dbc41
     * desc: PostingAmountEqual, Text
     */
    it("Text") {
      val filterTextStr =
        """>Filter:
          |>  AND
          |>    Posting Amount
          |>      account: "(abc.*)|(def.*)"
          |>      amount == 1
          |>    AND
          |>      Posting Amount
          |>        account: "xyz"
          |>        amount == 2
          |>      All pass
          |""".stripMargin

      val tfd = TxnFilterDefinition(
        TxnFilterAND(List[TxnFilter](
          TxnFilterPostingAmountEqual("(abc.*)|(def.*)",1),
          TxnFilterAND(List[TxnFilter](
            TxnFilterPostingAmountEqual("xyz", 2),
            txnFilterTrue
          )),
        )),
      )

      val text = tfd.text(">").mkString("", "\n", "\n")
      assert(filterTextStr === text)
    }
  }

  describe("PostingAmountLess") {
    /**
     * test: 3dbd4103-66ee-4747-8eae-75d6b13bdb29
     * desc: PostingAmountLess, JSON
     */
    it("JSON") {
      val filterJsonStr =
        """{"txnFilter":{"TxnFilterPostingAmountLess":{"regex":"(abc.*)|(def.*)","amount":1}}}"""

      val filterTextStr =
        """Filter:
          |  Posting Amount
          |    account: "(abc.*)|(def.*)"
          |    amount < 1
          |""".stripMargin

      val txnFilterRoot: Either[circe.Error, TxnFilterDefinition] = decode[TxnFilterDefinition](filterJsonStr)

      val tf = getFilter(txnFilterRoot)

      assert(filterTextStr === tf.text("").mkString("", "\n", "\n"))
      assert(filterJsonStr === tf.asJson.noSpaces)
    }

    /**
     * test: c0725d0c-2261-4a98-982f-4a62c4f9c7da
     * desc: PostingAmountLess, Text
     */
    it("Text") {
      val filterTextStr =
        """>Filter:
          |>  AND
          |>    Posting Amount
          |>      account: "(abc.*)|(def.*)"
          |>      amount < 1
          |>    AND
          |>      Posting Amount
          |>        account: "xyz"
          |>        amount < 2
          |>      All pass
          |""".stripMargin

      val tfd = TxnFilterDefinition(
        TxnFilterAND(List[TxnFilter](
          TxnFilterPostingAmountLess("(abc.*)|(def.*)", 1),
          TxnFilterAND(List[TxnFilter](
            TxnFilterPostingAmountLess("xyz", 2),
            txnFilterTrue
          )),
        )),
      )

      val text = tfd.text(">").mkString("", "\n", "\n")
      assert(filterTextStr === text)
    }
  }

  describe("PostingAmountGreater") {
    /**
     * test: 66d6ee10-a18e-4615-9e7a-1569c793fe46
     * desc: PostingAmountGreater, JSON
     */
    it("JSON") {
      val filterJsonStr =
        """{"txnFilter":{"TxnFilterPostingAmountGreater":{"regex":"(abc.*)|(def.*)","amount":1}}}"""

      val filterTextStr =
        """Filter:
          |  Posting Amount
          |    account: "(abc.*)|(def.*)"
          |    amount > 1
          |""".stripMargin

      val txnFilterRoot: Either[circe.Error, TxnFilterDefinition] = decode[TxnFilterDefinition](filterJsonStr)

      val tf = getFilter(txnFilterRoot)

      assert(filterTextStr === tf.text("").mkString("", "\n", "\n"))
      assert(filterJsonStr === tf.asJson.noSpaces)
    }

    /**
     * test: f940a623-f4b6-4937-86ff-c05ddc1921d6
     * desc: PostingAmountGreater, Text
     */
    it("Text") {
      val filterTextStr =
        """>Filter:
          |>  AND
          |>    Posting Amount
          |>      account: "(abc.*)|(def.*)"
          |>      amount > 1
          |>    AND
          |>      Posting Amount
          |>        account: "xyz"
          |>        amount > 2
          |>      All pass
          |""".stripMargin

      val tfd = TxnFilterDefinition(
        TxnFilterAND(List[TxnFilter](
          TxnFilterPostingAmountGreater("(abc.*)|(def.*)", 1),
          TxnFilterAND(List[TxnFilter](
            TxnFilterPostingAmountGreater("xyz", 2),
            txnFilterTrue
          )),
        )),
      )

      val text = tfd.text(">").mkString("", "\n", "\n")
      assert(filterTextStr === text)
    }
  }

  describe("PostingCommodity") {
    /**
     * test: b7b43b0f-0046-4d25-8f61-2ef419b84f0b
     * desc: PostingCommodity, JSON
     */
    it("JSON") {
      val filterJsonStr =
        """{"txnFilter":{"TxnFilterPostingCommodity":{"regex":"(abc.*)|(def.*)"}}}"""

      val filterTextStr =
        """Filter:
          |  Posting Commodity: "(abc.*)|(def.*)"
          |""".stripMargin

      val txnFilterRoot: Either[circe.Error, TxnFilterDefinition] = decode[TxnFilterDefinition](filterJsonStr)

      val tf = getFilter(txnFilterRoot)

      assert(filterTextStr === tf.text("").mkString("", "\n", "\n"))
      assert(filterJsonStr === tf.asJson.noSpaces)
    }

    /**
     * test: 15d83e84-11a6-4ec2-a458-82fea493f10f
     * desc: PostingCommodity, Text
     */
    it("Text") {
      val filterTextStr =
        """>Filter:
          |>  AND
          |>    Posting Commodity: "(abc.*)|(def.*)"
          |>    AND
          |>      Posting Commodity: "xyz"
          |>      All pass
          |""".stripMargin

      val tfd = TxnFilterDefinition(
        TxnFilterAND(List[TxnFilter](
          TxnFilterPostingCommodity("(abc.*)|(def.*)"),
          TxnFilterAND(List[TxnFilter](
            TxnFilterPostingCommodity("xyz"),
            txnFilterTrue
          )),
        )),
      )

      val text = tfd.text(">").mkString("", "\n", "\n")
      assert(filterTextStr === text)
    }
  }
}
