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

package fi.e257.tackler.model

import fi.e257.tackler.api.TxnHeader
import fi.e257.tackler.math.TacklerReal
import org.scalatest.funspec.AnyFunSpec

import java.time.ZonedDateTime

class TransactionTest extends AnyFunSpec {

  it ("txn_to_string") {
  val ts = ZonedDateTime.parse("2023-02-04T14:03:05.047974+02:00")

  val tests = List[(() => Transaction, String)](

    (() => {
      val atn_a = AccountTreeNode("a:b", None)
      val atn_e = AccountTreeNode("e:f", None)
      val e_post = Posting(atn_e, TacklerReal(1), TacklerReal(0), false, None, None)
      val a_post = Posting(atn_a, TacklerReal(-1), TacklerReal(0), false, None, None)
      val posts = List(e_post, a_post)
      Transaction(TxnHeader(ts, None, Some("desc"), None, None, None, None), posts)
    },
      """2023-02-04T14:03:05.047974+02:00 'desc
        |   e:f   1
        |   a:b  -1
        |""".stripMargin),
    )

    val count = tests.map(test => {

      val txn_str = test._1().toString()
      assert(txn_str === test._2)
      1
    }).foldLeft(0)(_ + _)

    assert(count === 1)
  }
}
