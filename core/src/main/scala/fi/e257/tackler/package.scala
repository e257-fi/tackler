/*
 * Copyright 2018 E257.FI
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
package fi.e257

/**
 * [[https://gitlab.com/e257/tackler Tackler]] core library
 *
 * = Base components =
 *  - [[fi.e257.tackler.core]] Core functionality
 *  - [[fi.e257.tackler.model]] Data models
 *  - [[fi.e257.tackler.parser]] Parser and utilities
 *  - [[fi.e257.tackler.report]] Reporting utilites, reports and exports
 *
 * == How to use Tackler programmatically ==
 *
 *  - Acquire settings: [[fi.e257.tackler.core.Settings$]]
 *  - Create sequence of Txns: [[fi.e257.tackler.parser.TacklerTxns]]
 *  - Settings for reports, e.g. [[fi.e257.tackler.report.BalanceSettings]]
 *  - Create actual report by [[fi.e257.tackler.report.BalanceReporter]]
 *  - Acquire report
 *    - json object: [[fi.e257.tackler.report.BalanceReporter.jsonReport]]
 *    - io-output: [[fi.e257.tackler.report.BalanceReporter.writeReport]]
 */
package object tackler {

}
