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
package fi.e257.tackler.math

import java.math.MathContext

object TacklerReal {

  private def mathContext: MathContext = MathContext.UNLIMITED

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def apply(i: Int): BigDecimal = BigDecimal(i, mathContext)

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def apply(l: Long): BigDecimal = BigDecimal(l, mathContext)

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def apply(d: Double): BigDecimal = BigDecimal(d, mathContext)

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def apply(x: String): BigDecimal = BigDecimal(x, mathContext)
}
