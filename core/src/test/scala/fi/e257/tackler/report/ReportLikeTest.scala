/*
 * Copyright 2016-2018 E257.FI
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

import io.circe.Json
import org.scalatest.FlatSpec
import fi.e257.tackler.model.TxnData

class ReportLikeTest extends FlatSpec {

  class DefaultReportSettings extends ReportConfiguration {
    override val minScale = 2
    override val maxScale = 7
  }

  class Frmt(val name: String, cfg: ReportConfiguration) extends ReportLike(cfg) {
    override def writeReport(formats: Formats, txns: TxnData): Unit = ???
    override def jsonReport(txnData: TxnData): Json = ???
  }

  val defaultFrmt = new Frmt("", new DefaultReportSettings())

  val sc0: scala.math.BigDecimal = 1
  val sc1: scala.math.BigDecimal = 1.1
  val sc2: scala.math.BigDecimal = 1.12
  val sc3: scala.math.BigDecimal = 1.123
  val sc6: scala.math.BigDecimal = 1.123456
  val sc8_tr7: scala.math.BigDecimal = 1.12345677
  val sc8_tr5: scala.math.BigDecimal = 1.12345675
  val sc8_tr4: scala.math.BigDecimal = 1.12345674

  val sc10_2: scala.math.BigDecimal = 1234567890.12
  val sc14_4: scala.math.BigDecimal = 12345678901234.1234

  val sc18_2 = BigDecimal(123456789123456789l) + BigDecimal(.12)
  val sc18_9 = BigDecimal("123456789123456789.123456789")

  //                                                                 1         2         3         4         5         6         7         8         9        10        11        12        13
  val sc30_130 = BigDecimal("123456789012345678901234567890.0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789")

  behavior of "scaleFrmtStr"

  it should "DIR with minScale" in {
    assert(defaultFrmt.getScaleFormat(sc0) === ".2f")
    assert(defaultFrmt.getScaleFormat(sc1) === ".2f")
  }

  it should "DIR between min and max" in {
    assert(defaultFrmt.getScaleFormat(sc2) === ".2f")
    assert(defaultFrmt.getScaleFormat(sc3) === ".3f")
    assert(defaultFrmt.getScaleFormat(sc6) === ".6f")
  }

  it should "DIR with maxScale" in {
    assert(defaultFrmt.getScaleFormat(sc8_tr7) === ".7f")
  }

  behavior of "valueFillFrmtStr"

  it should "DIR with small and big values" in {
    assert(defaultFrmt.getFillFormat(5, sc1) === "%5.2f")
    assert(defaultFrmt.getFillFormat(5, sc3) === "%5.3f")
    assert(defaultFrmt.getFillFormat(18, sc8_tr7) === "%18.7f")
    assert(defaultFrmt.getFillFormat(26, sc18_9) === "%26.7f")
  }

  behavior of "fillFormat"

  /**
   * test: 52a72e6e-0d5d-4620-af1c-c6edf0143d82
   */
  it should "format normal values" in {
    assert(defaultFrmt.scaleFormat(sc0) === "1.00")
    assert(defaultFrmt.scaleFormat(sc6) === "1.123456")
    assert(defaultFrmt.scaleFormat(sc10_2) === "1234567890.12")

    assert(defaultFrmt.fillFormat(1, sc0) === "1.00")
    assert(defaultFrmt.fillFormat(4, sc0) === "1.00")
    assert(defaultFrmt.fillFormat(5, sc0) === " 1.00")

    assert(defaultFrmt.fillFormat(9, sc6) === " 1.123456")
    assert(defaultFrmt.fillFormat(14, sc10_2) === " 1234567890.12")

  }

  /**
   * test: be4cec3b-b025-4dbd-9331-e78896843f04
   */
  it should "truncate values correctly" in {
    assert(defaultFrmt.scaleFormat(sc8_tr7) === "1.1234568")
    assert(defaultFrmt.scaleFormat(sc8_tr5) === "1.1234568")
    assert(defaultFrmt.scaleFormat(sc8_tr4) === "1.1234567")

    assert(defaultFrmt.fillFormat(10, sc8_tr7) === " 1.1234568")
    assert(defaultFrmt.fillFormat(10, sc8_tr5) === " 1.1234568")
    assert(defaultFrmt.fillFormat(10, sc8_tr4) === " 1.1234567")
  }

  /**
   * test: 77f9a99e-ef0a-47c4-a8c9-59f3d4478f31
   */
  it should "format large value" in {
    assert(defaultFrmt.scaleFormat(sc18_2) === "123456789123456789.12")
    assert(defaultFrmt.scaleFormat(sc18_9) === "123456789123456789.1234568")

    assert(defaultFrmt.fillFormat(22, sc18_2) === " 123456789123456789.12")
    assert(defaultFrmt.fillFormat(27, sc18_9) === " 123456789123456789.1234568")
  }

  /**
   * test: 1cf0c2c7-35a9-42b3-b916-8d3a20a9d428
   */
  it should "format, truncate and round with very large numbers (30 digits) with high precision (128 decimals)" in {
    class LargeScale extends ReportConfiguration {
      override val minScale = 2
      override val maxScale = 128
    }
    val largeFrmt = new Frmt("", new LargeScale())

    assert(largeFrmt.fillFormat(160, sc30_130) === " 123456789012345678901234567890.01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234568")
  }

  /**
   * test: f82c5cbc-2f8b-4c81-9732-36e85807b754
   */
  it should "verify rounding mode HALF_UP 1/2" in {
    class RoundingScale extends ReportConfiguration {
      override val minScale = 0
      override val maxScale = 0
    }
    val frmt = new Frmt("", new RoundingScale())

    // https://docs.oracle.com/javase/8/docs/api/java/math/RoundingMode.html
    val valrefs: List[(scala.math.BigDecimal, String)] = List(
      (BigDecimal( "5.5"), " 6"),
      (BigDecimal( "2.5"), " 3"),
      (BigDecimal( "1.6"), " 2"),
      (BigDecimal( "1.1"), " 1"),
      (BigDecimal( "1.0"), " 1"),
      (BigDecimal("-1.0"), "-1"),
      (BigDecimal("-1.1"), "-1"),
      (BigDecimal("-1.6"), "-2"),
      (BigDecimal("-2.5"), "-3"),
      (BigDecimal("-5.5"), "-6"),
    )

    valrefs.foreach({case (value, ref) =>
      assert(frmt.fillFormat(2, value) === ref)
    })
  }

  /**
   * test: bfa20914-bd4a-431e-879b-1d3abf2b9df3
   */
  it should "verify rounding mode HALF_UP 2/2" in {
    class DecimalRoundingScale extends ReportConfiguration {
      override val minScale = 2
      override val maxScale = 2
    }
    val frmt = new Frmt("", new DecimalRoundingScale())

    // https://docs.oracle.com/javase/8/docs/api/java/math/RoundingMode.html
    val valrefs: List[(scala.math.BigDecimal, String)] = List(
      (BigDecimal( "0.055"), " 0.06"),
      (BigDecimal( "0.025"), " 0.03"),
      (BigDecimal( "0.016"), " 0.02"),
      (BigDecimal( "0.011"), " 0.01"),
      (BigDecimal( "0.010"), " 0.01"),
      (BigDecimal("-0.010"), "-0.01"),
      (BigDecimal("-0.011"), "-0.01"),
      (BigDecimal("-0.016"), "-0.02"),
      (BigDecimal("-0.025"), "-0.03"),
      (BigDecimal("-0.055"), "-0.06"),
    )

    valrefs.foreach({case (value, ref) =>
      assert(frmt.fillFormat(5, value) === ref)
    })
  }
}
