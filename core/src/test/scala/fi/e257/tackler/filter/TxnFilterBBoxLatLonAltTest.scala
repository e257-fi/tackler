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

import fi.e257.tackler.api.{GeoPoint, TxnFilterBBoxLatLonAlt, TxnFilterDefinition, TxnHeader}
import fi.e257.tackler.model.Transaction
import io.circe.parser.decode
import org.scalatest.funspec.AnyFunSpec

class TxnFilterBBoxLatLonAltTest extends AnyFunSpec with TxnFilterBBoxSpec {

  describe("BBox 3D (Latitude, Longitude, Altitude)") {

    /**
     * test: 00d5f743-4eca-4d06-a5e5-4de035909828
     */
    it("Filter 2D Txns") {
      val geo2DTxnStr =
        s"""
           |2019-02-02 'Helsinki
           | # uuid: ${geo03}
           | # location: geo:60.170833,24.9375
           | e 1
           | a
           |
           |""".stripMargin

      val geo2DTxnData = tt.string2Txns(geo2DTxnStr)

      val txnFilter = TxnFilterBBoxLatLonAlt(
        40, 20, -2000, 65, 26, 14000
      )

      val txnData = geo2DTxnData.filter(TxnFilterDefinition(txnFilter))

      assert(txnData.txns.size === 0)
    }

    /**
     * test: 607d4e0e-e05b-43cf-87b6-d3cad309be73
     */
    it ("Filter 3D Txns") {

      val geo3DTxnData = tt.string2Txns(geo3DTxnStr)

      val txnFilter = TxnFilterBBoxLatLonAlt(
        40, 20,-2000, 65, 26, 14000,
      )

      val txnData = geo3DTxnData.filter(TxnFilterDefinition(txnFilter))

      assert(txnData.txns.size === 1)
      assert(checkUUID(txnData, geo03))
    }
  }

  describe("BBox 3D errors") {
    /**
     * test: 1d6f4fb9-bcfd-41ae-8720-2584ec2f4087
     */
    it("detects illegal arguments") {
      val errBBoxes: List[(BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal, String)] =
        List(
          (65.0, 0, 0, 40, 0, 0, "North is below South. South: 65.0; North: 40"),
          (-2, 0, 0, -30.0, 0, 0, "North is below South. South: -2; North: -30.0"),
          (25, 0, 0, -25, 0, 0, "North is below South. South: 25; North: -25"),

          (0, 0, 10, 0, 0, 8.1, "height is less than depth. Depth: 10; Height: 8.1"),
          (0, 0, -8.1, 0, 0, -10, "height is less than depth. Depth: -8.1; Height: -10"),
          (0, 0, 2, 0, 0, -2, "height is less than depth. Depth: 2; Height: -2"),

          (-90.1, 0, 0, 0, 0, 0, "South is beyond pole. South: -90.1"),
          (0, 0, 0, 90.1, 0, 0, "North is beyond pole. North: 90.1"),

          (0, -180.1, 0,  0, 0, 0, "West is beyond 180th Meridian. West: -180.1"),
          (0, 180.1, 0,  0, 0, 0, "West is beyond 180th Meridian. West: 180.1"),

          (0, 0, 0, 0, 180.1, 0, "East is beyond 180th Meridian. East: 180.1"),
          (0, 0, 0, 0, -180.1, 0, "East is beyond 180th Meridian. East: -180.1"),

          (0, 0, -6378137.1, 0, 0, -2, "Depth is beyond center of Earth. Depth: -6378137.1"),
        )

      val count = errBBoxes.map(errBBox => {
        val ex = intercept[IllegalArgumentException]({
          val _ = TxnFilterBBoxLatLonAlt(errBBox._1, errBBox._2, errBBox._3, errBBox._4, errBBox._5, errBBox._6)
        })

        assert(ex.getMessage.contains(errBBox._7))
        1
      }).foldLeft(0)(_ + _)

      assert(count === 13)
    }

    /**
     * test: 92232872-cea2-4787-8ba4-892d958796cb
     */
    it("detects illegal arguments via JSON") {
      val errBBoxFilterJson =
        """
          |{
          |  "txnFilter" : {
          |    "TxnFilterBBoxLatLonAlt" : {
          |      "south" : 0,
          |      "west" : 0,
          |      "depth" : 2,
          |      "north" : 60,
          |      "east" :  25,
          |      "height" : -2
          |    }
          |  }
          |}
        """.stripMargin

      val ex = intercept[IllegalArgumentException] {
        val _ = decode[TxnFilterDefinition](errBBoxFilterJson)
      }

      assert(ex.getMessage.contains("height is less than depth. Depth: 2; Height: -2"))
    }
  }

  describe("BBox 3D (Latitude, Longitude, Altitude) verification tests") {

    /**
     * test: 9aa6d324-3bcc-4fcd-ac75-2447f3a65d3b
     */
    it("Check edge cases (points and/or BBoxes)") {

      val count = geo2d3dTests.map(t => {
        val expectedCount = t._1

        val bbox = t._3
        val txnFilter = TxnFilterBBoxLatLonAlt(
          bbox._1, bbox._2, bbox._3, bbox._4, bbox._5, bbox._6
        )

        val tvecs = t._4

        val count = tvecs.map(v => {
          val geo = GeoPoint.toPoint(v._1, v._2, v._3).get
          val txn = Transaction(TxnHeader(date, None, None, None, Some(geo), None), posts)

          assert(TxnFilterBBoxLatLonAltF.filter(txnFilter, txn) === v._5)
          1
        }).foldLeft(0)(_ + _)

        assert(count === expectedCount, ", e.g. test vector size for one filter is wrong")
        1
      }).foldLeft(0)(_ + _)

      assert(count === 7, ", e.g. test count for filter is wrong")
    }

    /**
     * test: d6764e33-f20c-4c50-8452-d249d1f0c902
     */
    it("check altitude functionality") {

      val altTests = List[
        (Int, // test count
          (BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal), // 3D GEO Filter
          List[(BigDecimal, BigDecimal, Option[BigDecimal], Boolean)]) // Test vectors and result
        ](
        (4,
          (20, 10, 22, 45, 25, 22),
          List(
            (30, 15, Some(22), true),

            (30, 15, Some(-22), false),
            (30, 15, Some(22.1), false),
            (30, 15, Some(21.9), false),
          )
        ),
        (4,
          (20, 10, -22, 45, 25, -22),
          List(
            (30, 15, Some(-22), true),

            (30, 15, Some(22), false),
            (30, 15, Some(-22.1), false),
            (30, 15, Some(-21.9), false),
          )
        ),
        (7,
          (20, 10, -10, 45, 25, 10),
          List(
            (30, 15, Some(0), true),
            (30, 15, Some(5), true),
            (30, 15, Some(-5), true),
            (30, 15, Some(-10), true),
            (30, 15, Some(10), true),

            (30, 15, Some(-11), false),
            (30, 15, Some(11), false),
          )
        ),
        (6,
          (20, 10, -10, 45, 25, -1),
          List(
            (30, 15, Some(0), false),
            (30, 15, Some(-5), true),
            (30, 15, Some(-10), true),
            (30, 15, Some(-1), true),

            (30, 15, Some(-11), false),
            (30, 15, Some(0), false),
          )
        ),
      )

      val count = altTests.map(t => {
        val expectedCount = t._1

        val bbox = t._2
        val txnFilter = TxnFilterBBoxLatLonAlt(
          bbox._1, bbox._2, bbox._3, bbox._4, bbox._5, bbox._6
        )

        val tvecs = t._3

        val count = tvecs.map(v => {
          val geo = GeoPoint.toPoint(v._1, v._2, v._3).get
          val txn = Transaction(TxnHeader(date, None, None, None, Some(geo), None), posts)

          assert(TxnFilterBBoxLatLonAltF.filter(txnFilter, txn) === v._4)
          1
        }).foldLeft(0)(_ + _)

        assert(count === expectedCount, ", e.g. test vector size for one filter is wrong")
        1
      }).foldLeft(0)(_ + _)

      assert(count === 4, ", e.g. test count for filter is wrong")
    }
  }
}
