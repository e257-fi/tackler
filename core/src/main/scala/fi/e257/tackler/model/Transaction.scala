/*
 * Copyright 2016-2018 E257.FI
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

import java.time.ZonedDateTime

import fi.e257.tackler.api.{TxnHeader, TxnTS}
import fi.e257.tackler.core.TxnException
import fi.e257.tackler.math._

object OrderByTxn extends Ordering[Transaction] {
  def compare(before: Transaction, after: Transaction): Int = {
    before.compareTo(after)
  }
}

final case class Transaction(
  header: TxnHeader,
  posts: Posts) {

  if (!Posting.txnSum(posts).isZero) {
    throw new TxnException("TXN postings do not zero: " + Posting.txnSum(posts).toString())
  }

  /**
   * See TxnHeader on tackler-api for description of used logic.
   */
  def compareTo(otherTxn: Transaction): Int = {
    header.compareTo(otherTxn.header)
  }

  /**
   * Get header part of txn as string.
   *
   * @param indent indent string for meta and comment parts
   * @param tsFormatter timestamp formatter
   * @return new line terminated header of txn
   */
  def txnHeaderToString(indent: String, tsFormatter: (ZonedDateTime => String)): String = {
    header.txnHeaderToString(indent, tsFormatter)
  }

  override def toString: String = {
    val indent = " " * 3

    txnHeaderToString(indent, TxnTS.isoZonedTS) +
      posts.map(p => indent + p.toString).mkString("\n") + "\n"
  }
}
