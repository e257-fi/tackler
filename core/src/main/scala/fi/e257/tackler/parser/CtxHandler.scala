/*
 * Copyright 2017-2019 E257.FI
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
package fi.e257.tackler.parser

import java.time.{LocalDate, LocalDateTime, ZonedDateTime}

import cats.implicits._

import scala.collection.JavaConverters
import fi.e257.tackler.api.TxnHeader
import fi.e257.tackler.core.{AccountException, CfgKeys, CommodityException, Settings, TxnException}
import fi.e257.tackler.model.{AccountTreeNode, Commodity, Posting, Posts, Transaction, Txns}
import fi.e257.tackler.parser.TxnParser._
import org.slf4j.{Logger, LoggerFactory}

import scala.util.control.NonFatal

/**
 * Handler utilities for ANTLR Parser Contexts.
 *
 * These handlers convert Parser Contexts to
 * Tackler Model (to Transactions, Postings, etc).
 */
abstract class CtxHandler {
  val settings: Settings
  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  /**
   * Handle raw parser date productions,
   * and convert to ZonedDateTime (date -rule).
   *
   * @param dateCtx date productions
   * @return zoned ts
   */
  @SuppressWarnings(Array(
    "org.wartremover.warts.OptionPartial"))
  protected def handleDate(dateCtx: DateContext): ZonedDateTime = {

    val tzDate: ZonedDateTime =
      Option(dateCtx.TS_TZ()) match {
        case Some(tzTS) => {
          ZonedDateTime.parse(tzTS.getText,
            java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        }
        case None => {
          Option(dateCtx.TS()) match {
            case Some(localTS) => {
              val dt = LocalDateTime.parse(localTS.getText,
                java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
              ZonedDateTime.of(dt, settings.timezone)
            }
            case None => {
              val optDate = Option(dateCtx.DATE())
              require(optDate.isDefined) // IE if not

              val d = LocalDate.parse(optDate.get.getText,
                java.time.format.DateTimeFormatter.ISO_DATE)

              ZonedDateTime.of(d, settings.defaultTime, settings.timezone)
            }
          }
        }
      }
    tzDate
  }

  /**
   * Handle raw parser account entry (account -rule).
   *
   * @param accountCtx account context
   * @return Account tree node
   */
  @SuppressWarnings(Array(
    "org.wartremover.warts.TraversableOps",
    "org.wartremover.warts.ListOps"))
  protected def handleAccount(accountCtx: AccountContext, commodity: Option[Commodity]): AccountTreeNode = {

    val account: String = JavaConverters.asScalaIterator(accountCtx.children.iterator())
      .map(_.getText)
      .mkString("")

    if (settings.Accounts.strict) {
      settings.Accounts.coa.find({ case (key, _) => key === account }) match {
        case None =>
          val lineNro = accountCtx.start.getLine
          val msg = "Error on line: " + lineNro.toString + "; Account not found: [" + account + "]"
          log.error(msg)
          throw new AccountException(msg)
        case Some((_, value)) =>
          AccountTreeNode(value.account, commodity)
      }
    } else {
      AccountTreeNode(account, commodity)
    }
  }

  protected def handleAmount(amountCtx: AmountContext): BigDecimal = {
    BigDecimal(Option(amountCtx.INT()).getOrElse(amountCtx.NUMBER()).getText())
  }

  protected def handleClosingPosition(postingCtx: PostingContext): (
    BigDecimal,
    BigDecimal,
    Boolean,
    Option[Commodity],
    Option[Commodity]) = {

    val postCommodity = Option(postingCtx.opt_unit()).map(u => {
      Commodity(u.unit().getText)
    })

    /*
     * if txnCommodity (e.g. closing position) is not set, then use
     * posting commodity as txnCommodity.
     */
    val txnCommodity = Option(postingCtx.opt_unit()).flatMap(u => {

      Option(u.opt_position()).fold(Option(Commodity(u.unit().getText))){pos =>
        Option(pos.closing_pos()).map(cp => {
          // Ok, we have closing position, use its commodity
          Commodity(cp.unit().getText)
        })
      }
    })

    val postAmount = handleAmount(postingCtx.amount())

    val txnAmount = Option(postingCtx.opt_unit())
      .fold({
        (postAmount, false)
      }) { u =>
        Option(u.opt_position()).fold({
          (postAmount, false)
        }) { pos =>
          Option(pos.closing_pos()).fold({
            // plain value, no closing position
            (postAmount, false)
          })(cp => {
            // Ok, we have closing position
            Option(cp.AT()).fold({
              // this is '=', e.g. total price
              (handleAmount(cp.amount()), true)
            })(_ => {
              // this is '@', e.g. unit price
              (postAmount * handleAmount(cp.amount()), false)
            })
          })
        }
      }

    // todo: fix this silliness, see other todo on Posting
    (postAmount, txnAmount._1, txnAmount._2, postCommodity, txnCommodity)
  }

  /**
   * Check commodity if it's listed and in case of empty commodity,
   * if empty commodities are allowed.
   *
   * @param commodity optional commodity
   */
  protected def checkCommodity(commodity: Option[Commodity], lineNro: Int): Unit = {
    commodity match {
      case Some(c) => {
        if (!settings.Accounts.commodities.exists(_ === c.name)) {
          val msg = "Error on line: " + lineNro.toString + "; Commodity not found: [" + c.name + "]"
          log.error(msg)
          throw new CommodityException(msg)
        }
      }
      case None => {
        if (settings.Accounts.permit_empty_commodity === false) {
          val msg = "Error on line: " + lineNro.toString + "; Empty commodities are not allowed"
          log.error(msg)
          throw new CommodityException(msg)
        }
      }
    }
  }

  /**
   * Handle one Posting (posting -rule).
   *
   * @param postingCtx posting productions
   * @return Post
   */
  protected def handleRawPosting(postingCtx: PostingContext): Posting = {
    val foo = handleClosingPosition(postingCtx)

    if (settings.Accounts.strict) {
      val lineNro = postingCtx.start.getLine
      checkCommodity(foo._4, lineNro)
      checkCommodity(foo._5, lineNro)
    }

    val acctn = handleAccount(postingCtx.account(), foo._4)
    val comment = Option(postingCtx.opt_comment()).map(c => c.comment().text().getText)
    // todo: fix this silliness, see other todo on Posting
    Posting(acctn, foo._1, foo._2, foo._3, foo._5, comment)
  }

  /**
   * Handle one Transaction (txn -rule).
   *
   * @param txnCtx txn -productions
   * @return transaction
   */
  @SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
  protected def handleTxn(txnCtx: TxnContext): Transaction = {
    val date = handleDate(txnCtx.date())
    val code = Option(txnCtx.code()).map(c => c.code_value().getText.trim)

    val desc = Option(txnCtx.description()).fold[Option[String]](
      // No description at all
      None
    )(d => {
      // Ok, there was description
      // There is always "text" rule/token with current grammar (e.g. it can't be null).

      val s = d.text().getText

      // right-trim, there was quote on the left side ...
      Some(s.substring(0, s.lastIndexWhere(c => !c.isWhitespace) + 1))
    })


    val uuid = Option(txnCtx.txn_meta()).map(meta => {
      java.util.UUID.fromString(meta.txn_meta_uuid().UUID_VALUE().getText)
    })

    if (settings.Auditing.txnSetChecksum && uuid.isEmpty) {
      val msg = "" +
        "Configuration setting '" + CfgKeys.Auditing.txnSetChecksum + "' is activated and there is txn without UUID."
      log.error(msg)
      throw new TxnException(msg)
    }

    // txnCtx.txn_comment is never null, even when there aren't any comments
    // (in that case it will be an empty list)
    val comments = {
      val l = JavaConverters.asScalaIterator(txnCtx.txn_comment().iterator())
        .map(c => c.comment().text().getText).toList
      if (l.isEmpty) {
        None
      } else {
        Some(l)
      }
    }

    val posts: Posts =
      JavaConverters.asScalaIterator(txnCtx.postings().posting().iterator()).map(p => {
        handleRawPosting(p)
      }).toList

    // Check for mixed commodities
    if (posts.map(p => p.txnCommodity.map(c => c.name).getOrElse("")).distinct.size > 1) {
      val msg = "" +
        "Different commodities without value positions are not allowed inside single transaction." +
        uuid.map(u => "\n   txn uuid: " + u.toString).getOrElse("")
      log.error(msg)
      throw new CommodityException(msg)
    }

    val last_posting = Option(txnCtx.postings().last_posting()).map(lp => {
      // use same commodity as what other postings are using
      val ate = handleAccount(lp.account(), posts.head.txnCommodity)
      val amount = Posting.txnSum(posts)
      val comment = Option(lp.opt_comment()).map(c => c.comment().text().getText)

      List(Posting(ate, -amount, -amount, false, posts.head.txnCommodity, comment))
    })

    Transaction(TxnHeader(date, code, desc, uuid, comments), posts ++ last_posting.getOrElse(Nil))
  }

  /**
   * Handle multiple transaction productions (txns -rule).
   *
   * @param txnsCtx txns productions
   * @return sequence of Transactions.
   */
  protected def handleTxns(txnsCtx: TxnsContext): Txns = {
    JavaConverters.asScalaIterator(txnsCtx.txn().iterator())
      .map({ case (txnCtx) =>
        try {
          handleTxn(txnCtx)
        } catch {
          case NonFatal(ex) => {
            val lineNro = txnCtx.start.getLine
            log.error("Error while processing Transaction on line {}", lineNro.toString)
            throw ex
          }
        }
      }).toSeq
  }
}
