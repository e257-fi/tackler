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
package fi.e257.tackler.api

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import scala.util.{Failure, Success, Try}


/**
  * WGS84 (EPSG:4326) based Geo Location
  * This is based on simplified geo uri scheme, see TEP-1010 for details.
  *
  * @param lat Latitude in decimal degrees
  * @param lon Longitude in decimal degrees
  * @param alt optional altitude, in meters
  */
class GeoPoint protected (val lat: BigDecimal, val lon: BigDecimal, val alt: Option[BigDecimal]) {

  if (lat < -90 || 90 < lat) {
    throw new IllegalArgumentException("Value out of specification for Latitude: " + GeoPoint.frmt(lat))
  }
  if (lon < -180 || 180 < lon) {
    throw new IllegalArgumentException("Value out of specification for Longitude: " + GeoPoint.frmt(lon))
  }

  alt.foreach(z => {
    if (z < -6378137) {
      // Jules Verne: Voyage au centre de la Terre
      throw new IllegalArgumentException("Value Out of specification for Altitude: " + GeoPoint.frmt(z))
    }
  })

  override def toString: String = {
    "geo:" + GeoPoint.frmt(lat) + "," + GeoPoint.frmt(lon) + alt.map("," + GeoPoint.frmt(_)).getOrElse("")
  }
}

object  GeoPoint {
  def frmt(v: BigDecimal): String = {
    s"%.${v.scale}f".format(v)
  }

  /**
    * Create [[GeoPoint]] from given coordinates
    *
    * @param lat Latitude in decimal degrees
    * @param lon Longitude in decimal degrees
    * @param alt optional altitude, in meters
    * @return Success with GeoPoint or Failure
    */
  def toPoint(lat: BigDecimal, lon: BigDecimal, alt: Option[BigDecimal]): Try[GeoPoint] = {
    try {
      Success(new GeoPoint(lat, lon, alt))
    } catch {
      case ex: IllegalArgumentException => Failure[GeoPoint](ex)
    }
  }

  implicit val decodeTxnHeader: Decoder[GeoPoint] = deriveDecoder
  implicit val encodeTxnHeader: Encoder[GeoPoint] = deriveEncoder
}
