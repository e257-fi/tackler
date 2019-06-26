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
package fi.e257.tackler

/**
 * Scala 2.13.0 changes BigDecimal behaviour in breaking way
 * compared to 2.12 and 2.11
 *
 * See:
 * - https://github.com/scala/bug/issues/11592
 * - https://github.com/scala/bug/issues/11590
 */
package object math {
  type TacklerReal = scala.math.BigDecimal

  val ZERO = TacklerReal(0)


  implicit class TacklerSequenceOps(val seq: Seq[TacklerReal]) {

    /**
     * Calculate sum using correct MathContext
     *
     * This could be removed when StdLib sum is fixed:
     * https://github.com/scala/bug/issues/11592
     *
     * @return sum of items in sequence
     */
    def realSum: TacklerReal = seq.fold(TacklerReal(0))(_ + _)
  }

  implicit class TacklerBigDecimalOps(val r: BigDecimal) {

    /**
     * Check if value is zero, does NOT use epsilon
     *
     * @return true if value is zero
     */
    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
    def isZero: Boolean = r == ZERO
  }
}
