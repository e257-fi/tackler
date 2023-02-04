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

import org.scalatest.funspec.AnyFunSpec

import java.time.ZonedDateTime
import java.util.UUID

class TxnHeaderTest extends AnyFunSpec {

  private val ts = ZonedDateTime.parse("2023-02-04T14:03:05.047974+02:00")

  private val uuid_str = "ed6d4110-f3c0-4770-87fc-b99e46572244"
  private val uuid = UUID.fromString(uuid_str)

  private val geo = GeoPoint.toPoint(60.167, 24.955, Some(5)).get

  private val txnTags: List[String] = List("a", "b", "c", "a:b:c")

  private val comments = List("z 1st line", "c 2nd line", "a 3rd line")

  it ("tests") {
    TxnHeader(ts, None, None, None, None, None, None)
    val tests = List[(TxnHeader, String)](
      (TxnHeader(ts, None, None, None, None, None, None),
        s"""2023-02-04T14:03:05.047974+02:00
            |""".stripMargin),

      (
        TxnHeader(ts, Some("#123"), None, None, None, None, None),
        s"""2023-02-04T14:03:05.047974+02:00 (#123)
           |""".stripMargin),

      (TxnHeader(ts, Some("#123"), Some("desc"), None, None, None, None),
        s"""2023-02-04T14:03:05.047974+02:00 (#123) 'desc
           |""".stripMargin),

      (TxnHeader(ts, None, Some("desc"), None, None, None, None),
        s"""2023-02-04T14:03:05.047974+02:00 'desc
           |""".stripMargin),

      (TxnHeader(ts, None, Some("desc"), Some(uuid), None, None, None),
        s"""2023-02-04T14:03:05.047974+02:00 'desc
           |   # uuid: ${uuid_str}
           |""".stripMargin),

      (
        TxnHeader(ts, None, Some("desc"), None, Some(geo), None, None),
        s"""2023-02-04T14:03:05.047974+02:00 'desc
           |   # location: geo:60.167,24.955,5
           |""".stripMargin),

      (TxnHeader(ts, None, Some("desc"), None, None, Some(txnTags), None),
        s"""2023-02-04T14:03:05.047974+02:00 'desc
           |   # tags: a, b, c, a:b:c
           |""".stripMargin),

      (
        TxnHeader(ts, None, Some("desc"), None, None, None, Some(comments)),
        s"""2023-02-04T14:03:05.047974+02:00 'desc
           |   ; z 1st line
           |   ; c 2nd line
           |   ; a 3rd line
           |""".stripMargin),

      (
        TxnHeader(ts, Some("#123"), Some("desc"),  Some(uuid), Some(geo),  Some(txnTags), Some(comments)),
        s"""2023-02-04T14:03:05.047974+02:00 (#123) 'desc
           |   # uuid: ${uuid_str}
           |   # location: geo:60.167,24.955,5
           |   # tags: a, b, c, a:b:c
           |   ; z 1st line
           |   ; c 2nd line
           |   ; a 3rd line
           |""".stripMargin),
    )

    val count = tests.map(test => {

      val txn_str = test._1.txnHeaderToString(" " * 3, TxnTS.isoZonedTS)
      println("[" + txn_str + "]")
      assert(txn_str === test._2)
      1
    }).foldLeft(0)(_ + _)

    assert(count === 9)
    assert(count === tests.size)
  }
}
