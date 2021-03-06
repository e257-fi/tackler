/*
 * Copyright 2018-2019 E257.FI
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

import fi.e257.tackler.api.{BBoxLatLon, GeoPoint}
import fi.e257.tackler.model.Transaction

trait CanTxnFilter[A] {
  def filter(tf: A, txn: Transaction): Boolean
}

trait CanBBoxLatLonFilter[A <: BBoxLatLon] {
  def bbox2d(tf: A, geo: GeoPoint): Boolean = {
    if (tf.east < tf.west) {
      // BBox is over 180th meridian
      tf.south <= geo.lat && geo.lat <= tf.north &&
        (tf.west <= geo.lon || geo.lon <= tf.east)
    } else {
      tf.south <= geo.lat && geo.lat <= tf.north &&
        tf.west <= geo.lon && geo.lon <= tf.east
    }
  }
}
