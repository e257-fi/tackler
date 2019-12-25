/*
 * Copyright 2016-2019 E257.FI
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
package fi.e257.tackler.model

import fi.e257.tackler.core.TxnException
import fi.e257.tackler.math.TacklerReal
import org.scalatest.flatspec.AnyFlatSpec

class PostingTest extends AnyFlatSpec {

  val acctn = AccountTreeNode("a:b", None)

  behavior of "Posting"

  /**
   * test: 42ad9d32-64aa-4fcd-a4ab-1e8521b921e3
   */
  it should "not accept zero postings" in {
    assertThrows[TxnException]{
      Posting(acctn, TacklerReal(0), TacklerReal(0), false, None, None)
    }
    assertThrows[TxnException]{
      // check that difference precision doesn't mess up
      // bigdecimal comparisions
      Posting(acctn, TacklerReal(0.00), TacklerReal(0.00), false, None, None)
    }
  }

  /**
   * test: e3c97b66-318c-4396-8857-0cd2c1dfb0d2
   */
  it should "preserve precision" in {
   val v =
      //          3         2         1                   1         2         3         4
      TacklerReal("123456789012345678901234567890.123456789012345678901234567890123456789012")
    val p = Posting(acctn, v, v, false, None, None)

    assert(p.toString === "a:b   123456789012345678901234567890.123456789012345678901234567890123456789012")
  }

  /**
   * test: 6ce68af4-5349-44e0-8fbc-35bebd8ac1ac
   */
  it should "toString" in {
    val v = TacklerReal("123.01")
    val p = Posting(acctn, v, v, false, None, Some("comment"))

    assert(p.toString === "a:b   123.01 ; comment")
  }

  /**
    * test: 16b54e8c-5ea6-420c-bd72-157dbcc06a49
    */
  it should "handle unit price" in {
    val pv = TacklerReal("123.00")
    val tv = TacklerReal("246.00")
    val p = Posting(acctn, pv, tv, false, Some(Commodity("€")), None)

    assert(p.toString === "a:b   123.00 @ 2 €")
  }

  /**
    * test: 22059d1d-7c10-42dc-831f-03bd1f1d6257
    */
  it should "handle unit price with comment" in {
    val pv = TacklerReal("123.00")
    val tv = TacklerReal("246.00")
    val p = Posting(acctn, pv, tv, false, Some(Commodity("€")), Some("comment"))

    assert(p.toString === "a:b   123.00 @ 2 € ; comment")
  }

  /**
    * test: 0fef204a-19da-418f-b7d0-86b5211c2182
    */
  it should "handle total price" in {
    val pv = TacklerReal("123.00")
    val tv = TacklerReal("246.00")
    val p = Posting(acctn, pv, tv, true, Some(Commodity("€")), None)

    assert(p.toString === "a:b   123.00 = 246.00 €")
  }

  /**
    * test: 718dd25c-aebc-4f29-9903-67942c6ba531
    */
  it should "handle total price with comment" in {
    val pv = TacklerReal("123.00")
    val tv = TacklerReal("246.00")
    val p = Posting(acctn, pv, tv, true, Some(Commodity("€")), Some("comment"))

    assert(p.toString === "a:b   123.00 = 246.00 € ; comment")
  }
}