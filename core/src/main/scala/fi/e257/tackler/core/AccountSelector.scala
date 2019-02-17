/*
 * Copyright 2016-2019 E257.FI
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

import java.util.regex.Pattern

import cats.implicits._
import fi.e257.tackler.api.Checksum
import fi.e257.tackler.model.{AccumulatorPosting, BalanceTreeNode}


trait AccountSelector {
  val hash: Hash
  def checksum(): Checksum
}

abstract class RegexAccountSelector(patterns: Seq[String]) extends AccountSelector {
  private val trimmedPatterns = patterns.map(_.trim)

  protected val regexs = trimmedPatterns.map(name => {Pattern.compile(name)})

  protected val cs = hash.checksum(trimmedPatterns.sorted, "\n")

  override def checksum(): Checksum = cs
}

trait BalanceAccountSelector extends AccountSelector with Filtering[BalanceTreeNode]

/**
 * Select all Accounts on Balance Report.
 */
class AllBalanceAccounts(val hash: Hash) extends BalanceAccountSelector {
  override def predicate(x: BalanceTreeNode): Boolean = true

  override def checksum(): Checksum = {
    Checksum("None", "select all")
  }
}

/**
 * Filter Accounts on Balance Report based on account name,
 * e.g. select all accounts which match some of given patterns.
 *
 * @param patterns list of account name regexs
 */
class BalanceFilterByAccount(patterns: Seq[String], val hash: Hash) extends RegexAccountSelector(patterns) with BalanceAccountSelector {

  override def predicate(x: BalanceTreeNode): Boolean = {
    regexs.exists(_.matcher(x.acctn.account).matches())
  }
}

/**
 * Filter Accounts on Balance Report based on amount
 * e.g. select all accounts which have non-zero posting amount
 */
class BalanceFilterNonZero(val hash: Hash) extends BalanceAccountSelector {
  override def predicate(x: BalanceTreeNode): Boolean = {
    x.accountSum =!= 0
  }

  override def checksum(): Checksum = {
    Checksum("None", "select all non-zero")
  }
}

/**
 * Filter accounts based on name, and Non-Zero status
 * e.g. select all accounts which match regex, and have non-zero posting amount
 *
 * @param patterns list of account name regexs
 */
class BalanceFilterNonZeroByAccount(patterns: Seq[String], digest: Hash) extends BalanceFilterByAccount(patterns, digest) {
  override def predicate(x: BalanceTreeNode): Boolean = {
    super.predicate(x) && x.accountSum =!= 0
  }
}


trait RegisterAccountSelector extends AccountSelector with Filtering[AccumulatorPosting]

/**
 * Select all RegisterPostings (e.g. Account rows).
 */
class AllRegisterPostings(val hash: Hash)  extends RegisterAccountSelector {
  override def predicate(x: AccumulatorPosting): Boolean = true

  override def checksum(): Checksum = {
    Checksum("None", "select all")
  }
}

/**
 * Filter RegisterPostings based on account name,
 * e.g. select all RegisterPostings which match some
 * of given patterns.
 *
 * @param patterns list of account name regexs
 */
class RegisterFilterByAccount(patterns: Seq[String], val hash: Hash) extends RegexAccountSelector(patterns) with RegisterAccountSelector {
  override def predicate(x: AccumulatorPosting): Boolean = {
    regexs.exists(_.matcher(x.post.acctn.account).matches())
  }
}
