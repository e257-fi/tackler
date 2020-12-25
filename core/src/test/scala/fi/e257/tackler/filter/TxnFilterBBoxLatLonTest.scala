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

import fi.e257.tackler.api.{GeoPoint, TxnFilterBBoxLatLon, TxnFilterDefinition, TxnHeader}
import fi.e257.tackler.model.Transaction
import io.circe.parser.decode
import org.scalatest.funspec.AnyFunSpec

class TxnFilterBBoxLatLonTest extends AnyFunSpec with TxnFilterBBoxSpec {

  describe("BBox 2D (Latitude, Longitude) basic tests") {

    /**
     * test: 320d92b9-f8e7-4283-9296-74ff4340fff1
     */
    it("Filter 2D Txns") {
      val geo2DTxnData = tt.string2Txns(geo2DTxnStr)

      val txnFilter = TxnFilterBBoxLatLon(
        40, 20, 65, 26
      )

      val txnData = geo2DTxnData.filter(TxnFilterDefinition(txnFilter))

      assert(txnData.txns.size === 1)
      assert(checkUUID(txnData, geo03))
    }

    /**
     * test: 05983503-0aa4-42e1-a1c9-cc5df93285f7
     */
    it("Filter 3D Txns") {

      val geo3DTxnData = tt.string2Txns(geo3DTxnStr)

      val txnFilter = TxnFilterBBoxLatLon(
        40, 20, 65, 26
      )

      val txnData = geo3DTxnData.filter(TxnFilterDefinition(txnFilter))

      assert(txnData.txns.size === 2)
      assert(checkUUID(txnData, geo03))
      assert(checkUUID(txnData, geo04))
    }
  }

  describe("BBox 2D (Latitude, Longitude) error cases") {
    /**
     * test: 37063f39-0796-44bd-a300-511f36db8f48
     */
    it("detects illegal arguments") {
      val errBBoxes: List[(BigDecimal, BigDecimal, BigDecimal, BigDecimal, String)] =
        List(
          (65.0, 0, 40, 0, "North is below South. South: 65.0; North: 40"),
          (-2, 0, -30.0, 0, "North is below South. South: -2; North: -30.0"),
          (22, 0, -25, 0, "North is below South. South: 22; North: -25"),

          (-90.1, 0, 0, 0, "South is beyond pole. South: -90.1"),
          (0, 0, 90.1, 0,  "North is beyond pole. North: 90.1"),

          (0, -180.1, 0, 0, "West is beyond 180th Meridian. West: -180.1"),
          (0, 180.1, 0, 0, "West is beyond 180th Meridian. West: 180.1"),

          (0, 0, 0, 180.1,  "East is beyond 180th Meridian. East: 180.1"),
          (0, 0, 0, -180.1,  "East is beyond 180th Meridian. East: -180.1"),
        )

      val count = errBBoxes.map(errBBox => {
        val ex = intercept[IllegalArgumentException]({
          val _ = TxnFilterBBoxLatLon(errBBox._1, errBBox._2, errBBox._3, errBBox._4)
        })

        assert(ex.getMessage.contains(errBBox._5))
        1
      }).foldLeft(0)(_ + _)

      assert(count === 9)
    }

    /**
     * test: e690ce1d-4e0c-4f73-9b71-5a6a84dc52b8
     */
    it("detects illegal arguments via JSON") {
      val errBBoxFilterJson =
        """
          |{
          |  "txnFilter" : {
          |    "TxnFilterBBoxLatLon" : {
          |      "south" : 60,
          |      "west" : 0,
          |      "north" : 10,
          |      "east" : 0
          |    }
          |  }
          |}
        """.stripMargin

      val ex = intercept[IllegalArgumentException] {
        val _ = decode[TxnFilterDefinition](errBBoxFilterJson)
      }

      assert(ex.getMessage.contains("North is below South. South: 60; North: 10"))
    }
  }

  describe("BBox 2D (Latitude, Longitude) verification tests") {

    /**
     * test: 8f7e0c4e-a4b5-4f33-aad9-adaae1df1c5e
     */
    it ("Check edge cases (points and/or BBoxes)") {

      val count = geo2d3dTests.map(t => {
        val expectedCount = t._1

        val bbox = t._2
        val txnFilter = TxnFilterBBoxLatLon(
          bbox._1, bbox._2, bbox._3, bbox._4
        )

        val tvecs = t._4

        val count = tvecs.map(v => {
          val geo = GeoPoint.toPoint(v._1, v._2, v._3).get
          val txn = Transaction(TxnHeader(date, None, None, None, Some(geo), None, None), posts)

          assert(TxnFilterBBoxLatLonF.filter(txnFilter, txn) === v._4)
          1
        }).foldLeft(0)(_ + _)

        assert(count === expectedCount, ", e.g. test vector size for one filter is wrong")
        1
      }).foldLeft(0)(_ + _)

      assert(count === 7, ", e.g. test count for filter is wrong")
    }
  }
}
