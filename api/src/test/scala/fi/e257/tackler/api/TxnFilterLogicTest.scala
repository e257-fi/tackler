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

class TxnFilterLogicTest extends AnyFunSpecLike {

  def getFilter(json: Either[circe.Error, TxnFilterDefinition]): TxnFilterDefinition = {
    json.toOption.get
  }

  val txnFilterFalse = TxnFilterNone()
  val txnFilterTrue = TxnFilterAll()

  /**
   * test: caa264f6-719f-49e9-9b56-3bdf0b0941ec
   * desc: AND, JSON
   */
  it("AND: JSON") {
    val filterJsonStr =
      """{"txnFilter":{"TxnFilterAND":{"txnFilters":[{"TxnFilterAll":{}},{"TxnFilterNone":{}}]}}}"""

    val filterTextStr =
      """Filter:
        |  AND
        |    All pass
        |    None pass
        |""".stripMargin

    val txnFilterRoot: Either[circe.Error, TxnFilterDefinition] = decode[TxnFilterDefinition](filterJsonStr)

    val tf = getFilter(txnFilterRoot)

    assert(filterTextStr === tf.text("").mkString("", "\n", "\n"))
    assert(filterJsonStr === tf.asJson.noSpaces)
  }

  /**
   * test: deda9918-cba5-4b3d-85db-61a3a7e1128f
   * desc: AND, Text
   */
  it("AND: Text") {
    val filterTextStr =
      """>Filter:
        |>  AND
        |>    All pass
        |>    AND
        |>      All pass
        |>      None pass
        |""".stripMargin

    val tfd = TxnFilterDefinition(
      TxnFilterAND(List[TxnFilter](
        txnFilterTrue,
        TxnFilterAND(List[TxnFilter](
          txnFilterTrue,
          txnFilterFalse
        )),
      )),
    )

    val text = tfd.text(">").mkString("", "\n", "\n")
    assert(filterTextStr === text)
  }

  /**
   * test: eddb393f-b8a4-4189-9280-40a911417b70
   * desc: OR, JSON
   */
  it("OR: JSON") {
    val filterJsonStr =
      """{"txnFilter":{"TxnFilterOR":{"txnFilters":[{"TxnFilterAll":{}},{"TxnFilterNone":{}}]}}}"""

    val filterTextStr =
      """Filter:
        |  OR
        |    All pass
        |    None pass
        |""".stripMargin

    val txnFilterRoot: Either[circe.Error, TxnFilterDefinition] = decode[TxnFilterDefinition](filterJsonStr)
    val tf = getFilter(txnFilterRoot)

    assert(filterTextStr === tf.text("").mkString("", "\n", "\n"))
    assert(filterJsonStr === tf.asJson.noSpaces)
  }

  /**
   * test: 18959315-233a-4ede-8ec9-537951d45c6d
   * desc: OR, Text
   */
  it("OR: Text") {
    val filterTextStr =
      """>Filter:
        |>  OR
        |>    All pass
        |>    OR
        |>      All pass
        |>      None pass
        |""".stripMargin

    val tfd = TxnFilterDefinition(
      TxnFilterOR(List[TxnFilter](
        txnFilterTrue,
        TxnFilterOR(List[TxnFilter](
          txnFilterTrue,
          txnFilterFalse
        )),
      )),
    )

    val text = tfd.text(">").mkString("", "\n", "\n")
    assert(filterTextStr === text)
  }

  /**
   * test: 8416ffe5-f07b-4304-85ca-be3a3e15f5e7
   * desc: NOT, JSON
   */
  it("NOT: JSON") {
    val filterJsonStr =
      """{"txnFilter":{"TxnFilterNOT":{"txnFilter":{"TxnFilterNone":{}}}}}"""

    val filterTextStr =
      """Filter:
        |  NOT
        |    None pass
        |""".stripMargin

    val txnFilterRoot: Either[circe.Error, TxnFilterDefinition] = decode[TxnFilterDefinition](filterJsonStr)
    val tf = getFilter(txnFilterRoot)

    assert(filterTextStr === getFilter(txnFilterRoot).text("").mkString("", "\n", "\n"))
    assert(filterJsonStr === tf.asJson.noSpaces)
  }

  /**
   * test: 22482f84-2d21-48eb-8161-c16dfa8f9920
   * desc: NOT, Text
   */
  it("NOT: Text") {
    val filterTextStr =
      """>Filter:
        |>  NOT
        |>    NOT
        |>      All pass
        |""".stripMargin

    val tfd = TxnFilterDefinition(
      TxnFilterNOT(
        TxnFilterNOT(
          txnFilterTrue,
        )
      ),
    )

    val text = tfd.text(">").mkString("", "\n", "\n")
    assert(filterTextStr === text)
  }
}
