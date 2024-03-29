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
package fi.e257.tackler.filter

import org.scalatest.Assertions._

import fi.e257.tackler.model.TxnData

trait TxnFilterSpec  {
  def checkUUID(txnData: TxnData, uuid: String) = {
    txnData.txns.exists(txn => txn.header.uuid.map(u => u.toString).getOrElse("") === uuid)
  }

  def noErrors(txnData: TxnData, uuid: String) = {
    ! txnData.txns.exists(txn => txn.header.uuid.map(u => u.toString).getOrElse("") === uuid)
  }
}
