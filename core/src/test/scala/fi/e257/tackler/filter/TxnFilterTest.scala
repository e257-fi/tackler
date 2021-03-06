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
package fi.e257.tackler.filter

import java.time.ZonedDateTime

import fi.e257.tackler.api._
import fi.e257.tackler.model.Transaction
import org.scalatest.flatspec.AnyFlatSpec

class TxnFilterTest extends AnyFlatSpec {


  val txnFilterFalse = TxnFilterNone()
  val txnFilterTrue = TxnFilterAll()

  val txn = Transaction(TxnHeader(ZonedDateTime.now(), None, None, None, None, None, None), Seq.empty)

  behavior of "AND"

  /**
   * test: aa8aa459-b100-403e-98ea-7381ca58727d
   */
  it should "reject AND filter with only one filter" in {
    assertThrows[IllegalArgumentException] {
      val _ = TxnFilterAND(Seq[TxnFilter](txnFilterTrue))
    }
  }

  /**
   * test: 2bd7fa78-adda-4f35-93eb-9b602bb3667e
   */
  it should "AND(false, false)" in {
    val txnFilter = TxnFilterAND(List[TxnFilter](
      txnFilterFalse,
      txnFilterFalse))

    assert(txnFilter.filter(txn) === false)
  }

  /**
   * test: 11d4409c-93e2-4670-b2d5-65073980ba2d
   */
  it should "AND(false, true)" in {
    val txnFilter = TxnFilterAND(List[TxnFilter](
      txnFilterFalse,
      txnFilterTrue))

    assert(txnFilter.filter(txn) === false)
  }

  /**
   * test: 7635059e-1828-48f7-9799-5bb0d327f446
   */
  it should "AND(true, false)" in {
    val txnFilter = TxnFilterAND(List[TxnFilter](
      txnFilterTrue,
      txnFilterFalse))

    assert(txnFilter.filter(txn) === false)
  }

  /**
   * test: bd589c45-4c80-4ccd-9f2f-49caf964d2a5
   */
  it should "AND(true, true)" in {
    val txnFilter = TxnFilterAND(List[TxnFilter](
      txnFilterTrue,
      txnFilterTrue))

    assert(txnFilter.filter(txn) === true)
  }

  /**
   * test: feb1a75c-cea8-40db-b4bf-ef4d59d49c9e
   */
  it should "AND(true, false, true)" in {
    val txnFilter = TxnFilterAND(List[TxnFilter](
      txnFilterTrue,
      txnFilterFalse,
      txnFilterTrue
    ))

    assert(txnFilter.filter(txn) === false)
  }

  /**
   * test: 456c6b08-7e61-410b-8a36-c3c47d6355b0
   */
  it should "AND(true, true, false)" in {
    val txnFilter = TxnFilterAND(List[TxnFilter](
      txnFilterTrue,
      txnFilterTrue,
      txnFilterFalse
    ))

    assert(txnFilter.filter(txn) === false)
  }

  /**
   * test: 54cbd549-5567-4b19-bc20-a3de146fff40
   */
  it should "AND(filter, AND(...))" in {
    val txnFilter =
      TxnFilterAND(List[TxnFilter](
        txnFilterTrue,
        TxnFilterAND(List[TxnFilter](
          txnFilterFalse,
          txnFilterTrue
        ))
      ))

    assert(txnFilter.filter(txn) === false)
  }

  /**
   * test: 6e544624-ad3e-4920-9946-7eaf94febfb5
   */
  it should "AND(filter, OR(...))" in {
    val txnFilter =
      TxnFilterAND(List[TxnFilter](
        txnFilterTrue,
        TxnFilterOR(List[TxnFilter](
          txnFilterFalse,
          txnFilterTrue
        ))
      ))

    assert(txnFilter.filter(txn) === true)
  }

  /**
   * test: ef81d4c1-9d5e-47f2-ab7c-646fbc49e268
   */
  it should "AND(filter, NOT(...))" in {
    val txnFilter =
      TxnFilterAND(List[TxnFilter](
        txnFilterTrue,
        TxnFilterNOT(
          txnFilterFalse)
      ))

    assert(txnFilter.filter(txn) === true)
  }

  /**
   * test: b2e5d857-e02c-4313-9ca7-9aa765033343
   */
  it should "AND(AND(...), OR(...))" in {
    val txnFilter =
      TxnFilterAND(List[TxnFilter](
        TxnFilterAND(List[TxnFilter](
          txnFilterTrue,
          txnFilterTrue
        )),
        TxnFilterOR(List[TxnFilter](
          txnFilterFalse,
          txnFilterTrue
        ))
      ))

    assert(txnFilter.filter(txn) === true)
  }

  /**
   * test: dab44c95-834c-438a-8543-a73547284f03
   */
  it should "AND(filter, AND(...), OR(...), NOT(...))" in {
    val txnFilter =
      TxnFilterAND(List[TxnFilter](
        txnFilterTrue,
        TxnFilterAND(List[TxnFilter](
          txnFilterTrue,
          txnFilterTrue
        )),
        TxnFilterOR(List[TxnFilter](
          txnFilterFalse,
          txnFilterTrue
        )),
        TxnFilterNOT(
          txnFilterFalse
        )
      ))

    assert(txnFilter.filter(txn) === true)
  }


  behavior of "OR"

  /**
   * test: f9088d6f-d3ae-4120-b420-e77d0ea26f11
   */
  it should "reject OR filter with only one filter" in {
    assertThrows[IllegalArgumentException] {
      val _ = TxnFilterAND(Seq[TxnFilter](txnFilterTrue))
    }
  }


  /**
   * test: c6036b88-6032-4005-84d5-a9d29cc4b283
   */
  it should "OR(false, false)" in {
    val txnFilter = TxnFilterOR(List[TxnFilter](
      txnFilterFalse,
      txnFilterFalse))

    assert(txnFilter.filter(txn) === false)
  }

  /**
   * test: 0e03ed8a-23ad-48f1-af49-2b0967d573e3
   */
  it should "OR(false, true)" in {
    val txnFilter = TxnFilterOR(List[TxnFilter](
      txnFilterFalse,
      txnFilterTrue))

    assert(txnFilter.filter(txn) === true)
  }

  /**
   * test: 9aefdc26-b4bc-4e42-b0a8-ea2aefec7cde
   */
  it should "OR(true, false)" in {
    val txnFilter = TxnFilterOR(List[TxnFilter](
      txnFilterTrue,
      txnFilterFalse))

    assert(txnFilter.filter(txn) === true)
  }

  /**
   * test: ace886f3-a1cb-454e-9f7f-3c4c449a5ab2
   */
  it should "OR(true, true)" in {
    val txnFilter = TxnFilterOR(List[TxnFilter](
      txnFilterTrue,
      txnFilterTrue))

    assert(txnFilter.filter(txn) === true)
  }

  /**
   * test: 8b5afb02-b3f1-4b2b-a599-dda2f5b95884
   */
  it should "OR(false, true, false)" in {
    val txnFilter = TxnFilterOR(List[TxnFilter](
      txnFilterFalse,
      txnFilterTrue,
      txnFilterFalse
    ))

    assert(txnFilter.filter(txn) === true)
  }

  /**
   * test: 0666ff4f-88af-42af-b415-1b73658731c7
   */
  it should "OR(false, false, true)" in {
    val txnFilter = TxnFilterOR(List[TxnFilter](
      txnFilterFalse,
      txnFilterFalse,
      txnFilterTrue
    ))

    assert(txnFilter.filter(txn) === true)
  }

  /**
   * test: b75466f3-f7bf-4e7f-9865-e2937a5d968d
   */
  it should "OR(filter, AND(...))" in {
    val txnFilter =
      TxnFilterOR(List[TxnFilter](
        txnFilterFalse,
        TxnFilterAND(List[TxnFilter](
          txnFilterTrue,
          txnFilterTrue
        ))
      ))

    assert(txnFilter.filter(txn) === true)
  }

  /**
   * test: 9029ad79-bbea-4c0c-a0e0-09c8b1b04188
   */
  it should "OR(filter, OR(...))" in {
    val txnFilter =
      TxnFilterOR(List[TxnFilter](
        txnFilterFalse,
        TxnFilterOR(List[TxnFilter](
          txnFilterFalse,
          txnFilterTrue
        ))
      ))

    assert(txnFilter.filter(txn) === true)
  }

  /**
   * test: b01bfc0d-0f6d-409b-8101-4647c70d1409
   */
  it should "OR(filter, NOT(...))" in {
    val txnFilter =
      TxnFilterOR(List[TxnFilter](
        txnFilterFalse,
        TxnFilterNOT(
          txnFilterFalse)
      ))

    assert(txnFilter.filter(txn) === true)
  }

  /**
   * test: e8c40011-4aef-4639-98e2-1362a0961db8
   */
  it should "OR(AND(...), OR(...))" in {
    val txnFilter =
      TxnFilterOR(List[TxnFilter](
        TxnFilterAND(List[TxnFilter](
          txnFilterTrue,
          txnFilterTrue
        )),
        TxnFilterOR(List[TxnFilter](
          txnFilterFalse,
          txnFilterFalse
        ))
      ))

    assert(txnFilter.filter(txn) === true)
  }

  /**
   * test: 4b127707-c83b-418b-9703-849ee304a19c
   */
  it should "OR(filter, AND(...), OR(...), NOT(...))" in {
    val txnFilter =
      TxnFilterOR(List[TxnFilter](
        txnFilterFalse,
        TxnFilterAND(List[TxnFilter](
          txnFilterFalse,
          txnFilterTrue
        )),
        TxnFilterOR(List[TxnFilter](
          txnFilterFalse,
          txnFilterTrue
        )),
        TxnFilterNOT(
          txnFilterTrue)
      ))

    assert(txnFilter.filter(txn) === true)
  }




  behavior of "NOT"

  /**
   * test: 32aa1190-d5f2-40eb-a494-3cb7969ab65a
   */
  it should "NOT(false)" in {
    val txnFilter = TxnFilterNOT(
      txnFilterFalse
    )

    assert(txnFilter.filter(txn) === true)
  }

  /**
   * test: 08126158-2262-41f4-aa34-5695023d7a9b
   */
  it should "NOT(true)" in {
    val txnFilter = TxnFilterNOT(
      txnFilterTrue
    )

    assert(txnFilter.filter(txn) === false)
  }

  /**
   * test: b280271f-a0a6-41e1-aa49-305b9f4a791e
   */
  it should "NOT(NOT(...))" in {
    val txnFilter =
      TxnFilterNOT(
        TxnFilterNOT(
          txnFilterTrue
        ))

    assert(txnFilter.filter(txn) === true)
  }

  /**
   * test: 3e03d091-4f06-44d3-8bf5-285c85178ff9
   */
  it should "NOT(OR(...))" in {
    val txnFilter =
      TxnFilterNOT(
        TxnFilterOR(List[TxnFilter](
          txnFilterFalse,
          txnFilterTrue)))

    assert(txnFilter.filter(txn) === false)
  }

  /**
   * test: 0c549c6e-f3b7-4614-b874-31db1110c41c
   */
  it should "NOT(AND(...))" in {
    val txnFilter =
      TxnFilterNOT(
        TxnFilterAND(List[TxnFilter](
          txnFilterFalse,
          txnFilterTrue)))

    assert(txnFilter.filter(txn) === true)
  }
}
