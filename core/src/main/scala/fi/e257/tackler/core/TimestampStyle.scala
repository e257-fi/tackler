/*
 * Copyright 2020 E257.FI
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

sealed trait TimestampStyle

sealed case class DateTsStyle() extends TimestampStyle
sealed case class SecondsTsStyle() extends TimestampStyle
sealed case class FullTsStyle() extends TimestampStyle

object TimestampStyle {
  def apply(field: String): TimestampStyle = {
    field match {
      case CfgValues.`tsDateStyle` => DateTsStyle()
      case CfgValues.`tsSecondsStyle` => SecondsTsStyle()
      case CfgValues.`tsFullStyle` => FullTsStyle()
      /* Error*/
      case _ => throw new ConfigurationException(
        "Unknown timestamp field selector. Valid selectors are: " +
          CfgValues.tsDateStyle + ", " +
          CfgValues.tsSecondsStyle + ", " +
          CfgValues.tsFullStyle)
    }
  }
}
