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
package fi.e257.tackler.report

import fi.e257.tackler.model.TxnData
import io.circe.Json
import org.scalatest.FunSpec

class ReportLikeBigDecimalTest extends FunSpec {
  class DefaultReportSettings extends ReportConfiguration {
    override val minScale = 0
    override val maxScale = 25
  }

  class Frmt(val name: String, cfg: ReportConfiguration) extends ReportLike(cfg) {
    override def writeReport(formats: Formats, txns: TxnData): Unit = ???

    override def jsonReport(txnData: TxnData): Json = ???
  }

  val defaultFrmt = new Frmt("", new DefaultReportSettings())


  val bd1 = scala.math.BigDecimal("1000000000000000000000000.1")
  val bd2 = scala.math.BigDecimal("2000000000000000000000.0002")
  val bd3 = scala.math.BigDecimal("3000000000000000000.0000003")
  val bd4 = scala.math.BigDecimal("4000000000000000.0000000004")
  val bd5 = scala.math.BigDecimal("5000000000000.0000000000005")
  val bd6 = scala.math.BigDecimal("6000000000.0000000000000006")
  val bd7 = scala.math.BigDecimal("7000000.0000000000000000007")
  val bd8 = scala.math.BigDecimal("8000.0000000000000000000008")
  val bd9 = scala.math.BigDecimal("9.0000000000000000000000009")

  /**
   * Scala 2.13.0 breaks BigDecimal (e.g. this will fail with 2.13.0)
   */
  describe("Check BigDecimal") {

    it ("ok:  bd9") {
      val v = bd9
      val res = "9.0000000000000000000000009"

      assert(v.scale === 25)
      assert(v === scala.math.BigDecimal(res))
      assert(defaultFrmt.scaleFormat(v) === res)
    }

    it ("ok:  bd8 + bd9") {
      val v = bd8 + bd9
      val res = "8009.0000000000000000000008009"

      assert(v.scale === 25)
      assert(v === scala.math.BigDecimal(res))
      assert(defaultFrmt.scaleFormat(v) === res)
    }

    it ("ok:  bd7 + bd8 + bd9") {
      val v = bd7 + bd8 + bd9
      val res = "7008009.0000000000000000007008009"

      assert(v.scale === 25)
      assert(v === scala.math.BigDecimal(res))
      assert(defaultFrmt.scaleFormat(v) === res)
    }

    it ("ok:  bd6 + bd7 + bd8 + bd9") {
      val v = bd6 + bd7 + bd8 + bd9
      val res = "6007008009.0000000000000006007008009"

      assert(v.scale === 25)
      assert(v === scala.math.BigDecimal(res))
      assert(defaultFrmt.scaleFormat(v) === res)
    }

    it ("ok:  bd5 + bd6 + bd7 + bd8 + bd9") {
      val v = bd5 + bd6 + bd7 + bd8 + bd9
      val res = "5006007008009.0000000000005006007008009"

      assert(v.scale === 25)
      assert(v === scala.math.BigDecimal(res))
      assert(defaultFrmt.scaleFormat(v) === res)
    }

    it ("ok:  bd4 + bd5 + bd6 + bd7 + bd8 + bd9") {
      val v = bd4 + bd5 + bd6 + bd7 + bd8 + bd9
      val res = "4005006007008009.0000000004005006007008009"

      assert(v.scale === 25)
      assert(v === scala.math.BigDecimal(res))
      assert(defaultFrmt.scaleFormat(v) === res)
    }

    it ("ok:  bd3 + bd4 + bd5 + bd6 + bd7 + bd8 + bd9") {
      val v = bd3 + bd4 + bd5 + bd6 + bd7 + bd8 + bd9
      val res = "3004005006007008009.0000003004005006007008009"

      assert(v.scale === 25)
      assert(v === scala.math.BigDecimal(res))
      assert(defaultFrmt.scaleFormat(v) === res)
    }

    it ("ok:  bd2 + bd3 + bd4 + bd5 + bd6 + bd7 + bd8 + bd9") {
      val v = bd2 + bd3 + bd4 + bd5 + bd6 + bd7 + bd8 + bd9
      val res = "2003004005006007008009.0002003004005006007008009"

      assert(v.scale === 25)
      assert(v === scala.math.BigDecimal(res))
      assert(defaultFrmt.scaleFormat(v) === res)
    }

    it ("ok:  bd1 + bd2 + bd3 + bd4 + bd5 + bd6 + bd7 + bd8 + bd9") {
      val v = bd1 + bd2 + bd3 + bd4 + bd5 + bd6 + bd7 + bd8 + bd9
      val res = "1002003004005006007008009.1002003004005006007008009"

      assert(v.scale === 25)
      assert(v === scala.math.BigDecimal(res))
      assert(defaultFrmt.scaleFormat(v) === res)
    }
  }
}
