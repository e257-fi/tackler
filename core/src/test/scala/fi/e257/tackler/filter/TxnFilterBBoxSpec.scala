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

import fi.e257.tackler.core.Settings
import fi.e257.tackler.model.{AccountTreeNode, Posting}
import fi.e257.tackler.parser.TacklerTxns

trait TxnFilterBBoxSpec extends TxnFilterSpec {

  val tt = new TacklerTxns(Settings())

  val date = ZonedDateTime.parse("2019-05-04T12:00:00+03:00",
    java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)

  val posts = List[Posting](
    Posting(AccountTreeNode("e", None),  1,  1, false, None, None),
    Posting(AccountTreeNode("a", None), -1, -1, false, None, None))

  val geo00 = "cba1e5ed-37b2-4af1-9d07-6a21dec90f72"
  val geo01 = "1b3c507d-e22d-43bd-a49d-f110b2f2c6ad"
  val geo02 = "5180a076-2c55-4f97-9b17-38812977ce5f"
  val geo03 = "a18377c9-5a60-48fb-98aa-732903992ffe"
  val geo04 = "129cd2ea-8ba6-47a5-b67f-dccd2e7759fa"
  val geo05 = "bd388748-db9a-4db0-8ab2-1792a3827dec"

  val geo2DTxnStr =
    s"""
       |2019-01-01
       | # uuid: ${geo00}
       | ; no geo
       | e  1
       | a
       |
       |2019-01-01
       | # uuid: ${geo01}
       | # location: geo:0,0
       | e  1
       | a
       |
       |2019-02-01 'Stockholm
       | # uuid: ${geo02}
       | # location: geo:59.329444,18.068611
       | e  1
       | a
       |
       |2019-02-02 'Helsinki
       | # uuid: ${geo03}
       | # location: geo:60.170833,24.9375
       | e 1
       | a
       |
       |2019-03-01 'Nordcap
       | # uuid: ${geo05}
       | # location: geo:70.978056,25.974722
       | e  1
       | a
       |
       |""".stripMargin


  val geo3DTxnStr =
    s"""
       |2019-01-01
       | # uuid: ${geo00}
       | ; no geo
       | e  1
       | a
       |
       |2019-01-01
       | # uuid: ${geo01}
       | # location: geo:0,0,0
       | e  1
       | a
       |
       |2019-02-01 'Stockholm
       | # uuid: ${geo02}
       | # location: geo:59.329444,18.068611,0
       | e  1
       | a
       |
       |2019-02-02 'Helsinki
       | # uuid: ${geo03}
       | # location: geo:60.170833,24.9375,0
       | e 1
       | a
       |
       |2019-02-03 'Helsinki - no altitude
       | # uuid: ${geo04}
       | # location: geo:60.170833,24.9375
       | ; no Alt
       | e 1
       | a
       |
       |2019-03-01 'Nordcap
       | # uuid: ${geo05}
       | # location: geo:70.978056,25.974722,0
       | e  1
       | a
       |
       |""".stripMargin

  val geo2d3dTests = List[
    (Int, // test count
      (BigDecimal, BigDecimal, BigDecimal, BigDecimal), // 2D GEO Filter
      (BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal), // 3D GEO Filter
      List[(BigDecimal, BigDecimal, Option[BigDecimal], Boolean, Boolean)]) // Test vectors and 2D + 3D results
    ](
    (10,
      (20, 10, 45, 25),
      (20, 10, 22, 45, 25, 22),
      List(
        (30, 15, None, true, false),

        (18, 15, None, false, false),
        (50, 15, None, false, false),
        (30, 8, None, false, false),
        (30, 26, None, false, false),

        (30, 15, Some(22), true, true),

        (18, 15, Some(22), false, false),
        (50, 15, Some(22), false, false),
        (30, 8, Some(22), false, false),
        (30, 26, Some(22), false, false),
      )
    ),
    (10,
      (-16.5, -6, -15.75, -5.5),
      (-16.5, -6, -1000, -15.75, -5.5, 1000),
      List(
        (-15.97, -5.7, None, true, false),

        (-17, -5.7, None, false, false),
        (-15, -5.7, None, false, false),
        (-15.97, -7, None, false, false),
        (-15.97, -4, None, false, false),

        (-15.97, -5.7, Some(810), true, true),

        (-17, -5.7, Some(810), false, false),
        (-15, -5.7, Some(-5.75), false, false),
        (-15.97, -7, Some(-16), false, false),
        (-15.97, -4, Some(-810), false, false),
      )
    ),
    (12,
      // Special BBox around North pole
      (65, -180, 90, 180),
      (65, -180, 49, 90, 180, 51),
      List(
        (70.978056, 25.974722, None, true, false),
        (70.978056, -25.974722, None, true, false),
        (90, 25, None, true, false),
        (90, -25, None, true, false),

        (62,  25.974722, None, false, false),
        (-62, 25.974722, None, false, false),

        (70.978056, 25.974722, Some(50), true, true),
        (70.978056, -25.974722, Some(50), true, true),
        (90, 25, Some(50), true, true),
        (90, -25, Some(50), true, true),

        (62,  25.974722, Some(50), false, false),
        (-62, 25.974722, Some(50), false, false),
      )
    ),
    (12,
      // Special BBox around South pole
      (-90, -180, -65, 180),
      (-90, -180, -51, -65, 180, -49),
      List(
        (-70.978056, 25.974722, None, true, false),
        (-70.978056, -25.974722, None, true, false),
        (-90, 25, None, true, false),
        (-90, -25, None, true, false),

        (62,  25.974722, None, false, false),
        (-62, 25.974722, None, false, false),

        (-70.978056, 25.974722, Some(-50), true, true),
        (-70.978056, -25.974722, Some(-50), true, true),
        (-90, 25, Some(-50), true, true),
        (-90, -25, Some(-50), true, true),

        (62,  25.974722, Some(-50), false, false),
        (-62, 25.974722, Some(-50), false, false),
      )
    ),

    (6,
      // Special BBox around Fiji
      (-21, 175, -15, -175),
      (-21, 175, -100, -15, -175, 100),
      List(
        (-18.5, 178.56, None, true, false),

        (-18.5, 168, None, false, false),
        (-18.5, -174, None, false, false),

        (-18.5, 178.56, Some(50), true, true),

        (-18.5, 168, Some(50), false, false),
        (-18.5, 174, Some(50), false, false),
      )
    ),
    (13,
      // Special BBox around Earth
      (-90, -180, 90, 180),
      (-90, -180, 0, 90, 180, 1),

      List(
        (30, 15, Some(1), true, true),
        (0, 0, Some(1), true, true),
        (0, 180, Some(1), true, true),
        (0, -180, Some(1), true, true),

        (-15.97, -5.7, Some(1), true, true),


        (-70.978056, 25.974722, Some(1), true, true),
        (-70.978056, -25.974722, Some(1), true, true),
        (-90, 25, Some(1), true, true),
        (-90, -25, Some(1), true, true),

        (70.978056, 25.974722, Some(1), true, true),
        (70.978056, -25.974722, Some(1), true, true),
        (90, 25, Some(1), true, true),
        (90, -25, Some(1), true, true),
      )
    ),
    (2,
      // Special BBox: point
      (1, 2, 1, 2),
      (1, 2, 3, 1, 2, 3),

      List(
        (1, 2, None, true, false),
        (1, 2, Some(3), true, true),
      )
    ),

  )
}
