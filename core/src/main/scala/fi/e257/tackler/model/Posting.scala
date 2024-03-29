/*
 * Copyright 2016-2020 E257.FI
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
package fi.e257.tackler.model

import cats.syntax.all._
import fi.e257.tackler.core.TxnException
import fi.e257.tackler.math._

/**
 * Note about commodity support:
 * Native commodity of posting is commodity which is recorded by AccountTreeNode
 *
 * If there are multiple commodities in one transaction,
 * then there is conversion between these commodities.
 * Result of this conversion is called "txnAmount and txnCommodity"
 *
 * @param acctn account information
 * @param amount of this posting (in posting's own commodity)
 * @param txnAmount mixed commodity txn, this is amount as converted to Txn's commodity (see note)
 * @param isTotalAmount true if closing position was total amount, not unit price
 * @param txnCommodity mixed commodity txn, this txn's commodity (see note)
 * @param comment  of this posting, if any
 */
final case class Posting(
  acctn: AccountTreeNode,
  amount: TacklerReal,
  // todo: fix / rename these (position?, exchange? amount, commodity)
  txnAmount: TacklerReal,
  isTotalAmount: Boolean,
  txnCommodity: Option[Commodity],
  comment: Option[String]) {

  if (amount.isZero) {
    throw new TxnException("Zero sum postings are not allowed (is it typo?): " + acctn.account)
  }

  def atnKey: String = acctn.getFull

  override
  def toString: String = {
    val missingSign = if (amount < 0) "" else " "
    acctn.toString + "  " +
      missingSign + amount.toString() + acctn.commodity.map(c => " " + c.name).getOrElse("") +
      txnCommodity.map(txnC => {
        // todo: fix this
        if (txnC.name === acctn.commStr) {
          ""
        } else {
          if (isTotalAmount) {
            " = " + txnAmount.toString() + " " + txnC.name
          } else {
            " @ " + (txnAmount / amount).toString() + " " + txnC.name
          }
        }
      }).getOrElse("") +
      comment.map(c => " ; " + c).getOrElse("")
  }
}

object Posting {

  def txnSum(posts: Posts): TacklerReal = {
    posts.map(_.txnAmount).realSum
  }
}
