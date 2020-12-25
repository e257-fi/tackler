/*
 * Copyright 2017-2020 E257.FI
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
import java.util.UUID

import cats.syntax.all._
import fi.e257.tackler.api.{GeoPoint, Tags, TxnHeader}
import fi.e257.tackler.core._
import fi.e257.tackler.math.TacklerReal
import fi.e257.tackler.model._
import fi.e257.tackler.parser.TxnParser._
import org.antlr.v4.runtime.ParserRuleContext
import org.slf4j.{Logger, LoggerFactory}

import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

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
   * Get Parse Tree as string (accounts, tags, etc.)
   * ctx= "abc", ':', "def"  => "abc:def"
   *
   * @param ctx parse tree context holding items
   * @return items returned as strings
   */
  protected def contextToString(ctx: ParserRuleContext): String = {
    ctx.children.iterator().asScala
      .map(_.getText)
      .mkString("")
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

    val account: String = contextToString(accountCtx)

    if (settings.Accounts.strict) {
      if (!settings.Accounts.coa.exists({ case (key, _) => key === account })) {
          val lineNro = accountCtx.start.getLine
          val msg = "Error on line: " + lineNro.toString + "; Account not found: [" + account + "]"
          log.error(msg)
          throw new AccountException(msg)
      }
    }

    AccountTreeNode(account, commodity)
  }

  protected def handleTagCtx(tagCtx: TagContext): Tags = {

    val tag: String = contextToString(tagCtx)

    if (settings.Tags.strict) {
      if (! settings.Tags.cot.exists(_ === tag)) {
          val lineNro = tagCtx.start.getLine
          val msg = "Error on line: " + lineNro.toString + "; tag not found: [" + tag + "]"
          log.error(msg)
          throw new TagsException(msg)
      }
    }

    List(tag)
  }

  protected def handleAmount(amountCtx: AmountContext): TacklerReal = {
    TacklerReal(amountCtx.getText())
  }

  protected def handleValuePosition(postingCtx: PostingContext): (
    TacklerReal,
    TacklerReal,
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
          // Ok, we have value position, use its commodity
          val valPosCommodity = Commodity(cp.unit().getText)

          postCommodity.foreach(c => {
            // postCommodity is always defined by grammar
            if (c.name === valPosCommodity.name) {
              val lineNro = postingCtx.start.getLine
              val msg = "Error on line: " + lineNro.toString + "; Both commodities are same for value position [" + valPosCommodity.name + "]"
              log.error(msg)
              throw new CommodityException(msg)
            }
          })
          valPosCommodity
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
          Option(pos.opt_opening_pos()).foreach(opening_pos => {
            val opening_price = handleAmount(opening_pos.amount())
            if (opening_price < 0) {
              val lineNro = postingCtx.start.getLine
              val msg = "Error on line: " + lineNro.toString +
                "; Unit cost '{ ... }' is negative"
              log.error(msg)
              throw new CommodityException(msg)
            }
          })

          Option(pos.closing_pos()).fold({
            // plain value, no closing position
            (postAmount, false)
          })(cp => {
            // Ok, we have closing position
            Option(cp.AT()).fold({
              // this is '=', e.g. total price
              val total_cost = handleAmount(cp.amount())
              if ((total_cost < 0 && 0 <= postAmount) || (postAmount < 0 && 0 <= total_cost)) {
                val lineNro = postingCtx.start.getLine
                val msg = "Error on line: " + lineNro.toString +
                 "; Total cost '=' has different sign than primary posting value"
                log.error(msg)
                throw new CommodityException(msg)
              }
              (total_cost, true)
            })(_ => {
              // this is '@', e.g. unit price
              val unit_price = handleAmount(cp.amount())
              if (unit_price < 0) {
                val lineNro = postingCtx.start.getLine
                val msg = "Error on line: " + lineNro.toString +
                  "; Unit price '@' is negative"
                log.error(msg)
                throw new CommodityException(msg)
              }
              (postAmount * unit_price, false)
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


    val foo = handleValuePosition(postingCtx)

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

  @SuppressWarnings(Array(
    "org.wartremover.warts.Recursion"))
  protected def handleTagsCtx(tagsCtx: TagsContext): Tags = {
    // Tags parse tree ctx:
    //   tagsCtx.tag  always
    //   tagsCtx.tags sometimes (when multiple tags, recursive)
    //
    // See TxnParser.g4: 'txn_meta_tags' and 'tags' rules

    val tag = handleTagCtx(tagsCtx.tag())

    Option(tagsCtx.tags()).fold(
      tag
    ){ tagsTagsCtx =>
      handleTagsCtx(tagsTagsCtx) ++ tag
    }
  }

  protected def handleMeta(metaCtx: Txn_metaContext): (Option[UUID], Option[GeoPoint], Option[Tags]) = {

    val uuid:Option[UUID] = Option(metaCtx.txn_meta_uuid()).flatMap(muuid => {
      muuid.asScala.map(u => java.util.UUID.fromString(u.UUID_VALUE().getText)).headOption
      })

    val geo: (Option[GeoPoint]) = Option(metaCtx.txn_meta_location()).flatMap(geoCtxs => {
      geoCtxs.asScala.map(geoCtx => {
        GeoPoint.toPoint(
          TacklerReal(geoCtx.geo_uri().lat().getText),
          TacklerReal(geoCtx.geo_uri().lon().getText()),
          Option(geoCtx.geo_uri().alt()).map(a => TacklerReal(a.getText))
        ) match {
          case Success(g) => g
          case Failure(ex) => {
            log.error("Invalid geo-uri:" + ex.getMessage)
            throw new TacklerException("Invalid geo-uri: " + ex.getMessage)
          }
        }
      }).headOption
    })

    val tags = Option(metaCtx.txn_meta_tags()).flatMap(mtagsCtx => {
      mtagsCtx.asScala.map(c => handleTagsCtx(c.tags())).headOption
    })
    (uuid, geo, tags)
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


    val meta = Option(txnCtx.txn_meta())
      .fold[(Option[UUID], Option[GeoPoint], Option[List[String]])]((None, None, None))(metaCtx => {
      handleMeta(metaCtx)
    })
    val uuid: Option[UUID] = meta._1
    val geo: Option[GeoPoint] = meta._2
    val tags: Option[Tags] = meta._3

    if (settings.Auditing.txnSetChecksum && uuid.isEmpty) {
      val msg = "" +
        "Configuration setting '" + CfgKeys.Auditing.txnSetChecksum + "' is activated and there is txn without UUID."
      log.error(msg)
      throw new TxnException(msg)
    }

    // txnCtx.txn_comment is never null, even when there aren't any comments
    // (in that case it will be an empty list)
    val comments = {
      val l = txnCtx.txn_comment().iterator().asScala
        .map(c => c.comment().text().getText).toList
      if (l.isEmpty) {
        None
      } else {
        Some(l)
      }
    }

    val posts: Posts =
      txnCtx.postings().posting().iterator().asScala.map(p => {
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

    Transaction(TxnHeader(date, code, desc, uuid, geo, tags, comments), posts ++ last_posting.getOrElse(Nil))
  }

  /**
   * Handle multiple transaction productions (txns -rule).
   *
   * @param txnsCtx txns productions
   * @return sequence of Transactions.
   */
  protected def handleTxns(txnsCtx: TxnsContext): Txns = {
    txnsCtx.txn().iterator().asScala
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
