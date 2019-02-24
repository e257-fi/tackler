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
package fi.e257.tackler.core

import java.security.MessageDigest

import fi.e257.tackler.api.Checksum

final class Hash private (val algorithm: String) {

  /**
   * Calculate hash based checksum over list of items.
   *
   * @param items digest is calculated over this list of items
   * @param sep used to separated items
   * @return calculated checksum
   */
  def checksum(items: Seq[String], sep: String): Checksum = {
    Checksum(
      algorithm,
      items
        .foldLeft(MessageDigest.getInstance(algorithm))({
          case (md, item) => {
            md.update((item + sep).getBytes("UTF-8"))
            md
          }
        })
        .digest()
        .map(b => "%02x".format(0xff & b)).mkString
    )
  }
}

object Hash {

  /**
   * @param name of used hash algorithm
   */
  def apply(name: String): Hash = {
    // normalize hash name, and check/enforce valid algorithm early on
    new Hash(MessageDigest.getInstance(name).getAlgorithm)
  }
}
