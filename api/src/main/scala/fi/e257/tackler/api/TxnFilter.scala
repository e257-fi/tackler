/*
 * Copyright 2018-2019 E257.FI
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
package fi.e257.tackler.api

import java.time.ZonedDateTime
import java.util.UUID

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

// Circe with java8 time: https://github.com/circe/circe/issues/378
import io.circe.java8.time.decodeZonedDateTime
import io.circe.java8.time.encodeZonedDateTime

sealed trait TxnFilter {
  def text(indent: String): Seq[String]
}
object TxnFilter {
  @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
  implicit val decodeTxnFilter: Decoder[TxnFilter] = deriveDecoder[TxnFilter]

  @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
  implicit val encodeTxnFilter: Encoder[TxnFilter] = deriveEncoder[TxnFilter]
}

/**
 * Top-level container element of transaction filter definition.
 *
 * Transaction filter definitions can form tree-like structures,
 * and this is mandatory root node of transaction filter definition.
 *
 * @param txnFilter
 */
final case class TxnFilterDefinition(txnFilter: TxnFilter) {
  def text(indent: String): Seq[String] = {
    val myIndent = indent + "  "

    Seq(indent + "Filter:") ++
      txnFilter.text(myIndent)
  }
}
object TxnFilterDefinition {
  @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
  implicit val decodeTxnFilterDefinition: Decoder[TxnFilterDefinition] = deriveDecoder[TxnFilterDefinition]

  @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
  implicit val encodeTxnFilterDefinition: Encoder[TxnFilterDefinition] = deriveEncoder[TxnFilterDefinition]
}

/**
 * Deselects all transactions.
 */
final case class TxnFilterNone() extends TxnFilter {
  override def text(indent: String): Seq[String] = Seq(indent + "None pass")
}

/**
 * Selects all transactions.
 */
final case class TxnFilterAll() extends TxnFilter {
  override def text(indent: String): Seq[String] = Seq(indent + "All pass")
}


sealed trait TxnFilters extends TxnFilter {
  val txnFilters: Seq[TxnFilter]
  val opTxt: String

  require(txnFilters.size > 1, "There must be at least two TxnFilters")

  override def text(indent: String): Seq[String] = {
    val myIndent = indent + "  "
    Seq(indent +  opTxt) ++ txnFilters.flatMap(f => f.text(myIndent))
  }
}

/**
 * Logical AND, e.g. selects transaction if and only if all contained filters select it.
 *
 * @param txnFilters sequence of [[TxnFilter]] filters. There must be at least two filters.
 */
final case class TxnFilterAND(txnFilters: Seq[TxnFilter]) extends TxnFilters() {
  val opTxt = "AND"

}

/**
 * Logical OR, e.g. selects transaction any (or all) contained filters select it.
 *
 * @param txnFilters sequence of [[TxnFilter]] filters. There must be at least two filters.
 */
sealed case class TxnFilterOR(txnFilters: Seq[TxnFilter]) extends TxnFilters {
  val opTxt = "OR"
}

/**
 * Logical NOT, e.g. selects all deselected transactions, and deselect all selected transactions.
 *
 * @param txnFilter which will be negated.
 */
sealed case class TxnFilterNOT(txnFilter: TxnFilter) extends TxnFilter {
  override def text(indent: String): Seq[String] = {
    val myIndent = indent + "  "
    Seq(indent + "NOT") ++ txnFilter.text(myIndent)
  }

}

sealed abstract class TxnFilterTxnTS(ts: ZonedDateTime) extends TxnFilter {
  // Circe with java8 time: https://github.com/circe/circe/issues/378
  val opTxt: String

  override def text(indent: String): Seq[String] = {
    Seq(indent +  "Txn TS: " + opTxt + " " + TxnTS.isoZonedTS(ts))
  }
}

/**
 * Selects transaction if txn timestamp is on or after specified time.
 *
 * @param begin txn timestamp must be on or after this
 */
final case class TxnFilterTxnTSBegin(begin: ZonedDateTime) extends TxnFilterTxnTS(begin) {
  // Circe with java8 time: https://github.com/circe/circe/issues/378
  val opTxt = "begin"
}

/**
 * Selects transaction if txn timestamp is before of specified time.
 *
 * @param end txn timestamp is before of specified time.
 */
final case class TxnFilterTxnTSEnd(end: ZonedDateTime) extends TxnFilterTxnTS(end) {
  // Circe with java8 time: https://github.com/circe/circe/issues/378
  val opTxt = "end  "
}

sealed abstract class TxnFilterRegex(regex: String) extends TxnFilter {
  val rgx = java.util.regex.Pattern.compile(regex)

  val target: String

  override def text(indent: String): Seq[String] = {
    Seq(indent +  target + ": " + "\"" + s"${regex}" + "\"")
  }
}

/**
  * Select transaction if regular expression matches txn code.
  *
  * Used regular expression engine is java.util.regex.Pattern.
  *
  * @param regex to match txn code.
  */
final case class TxnFilterTxnCode(regex: String) extends TxnFilterRegex(regex) {
  val target = "Txn Code"
}

/**
 * Select transaction if regular expression matches txn description.
 *
 * Used regular expression engine is java.util.regex.Pattern.
 *
 * @param regex to match txn description
 */
final case class TxnFilterTxnDescription(regex: String) extends TxnFilterRegex(regex) {
  val target = "Txn Description"

}

/**
 * Select transaction if txn UUID is same as specified uuid.
 *
 * @param uuid
 */
final case class TxnFilterTxnUUID(uuid: UUID) extends TxnFilter {

  override def text(indent: String): Seq[String] = {
    Seq(indent +  "Txn UUID: " + uuid.toString)
  }
}

sealed abstract class BBoxLatLon extends TxnFilter {
  val south: BigDecimal
  val west: BigDecimal
  val north: BigDecimal
  val east: BigDecimal

  if (north < south) {
    throw new IllegalArgumentException("Invalid Bounding Box: North is below South. " +
      "South: " + GeoPoint.frmt(south) + "; North: " + GeoPoint.frmt(north))
  }

  // east < west case is needed for filter over 180th meridian

  // other pole is tested by north < south check
  if (south < -90) {
    throw new IllegalArgumentException("Invalid Bounding Box: South is beyond pole. " +
      "South: " + GeoPoint.frmt(south))
  }

  // other pole is tested by north < south check
  if (90 < north) {
    throw new IllegalArgumentException("Invalid Bounding Box: North is beyond pole. " +
      "North: " + GeoPoint.frmt(north))
  }

  if (west < -180 || 180 < west) {
    throw new IllegalArgumentException("Invalid Bounding Box: West is beyond 180th Meridian. " +
      "West: " + GeoPoint.frmt(west))
  }

  if (east < -180 || 180 < east) {
    throw new IllegalArgumentException("Invalid Bounding Box: East is beyond 180th Meridian. " +
      "East: " + GeoPoint.frmt(east))
  }
}

/**
 * Select Transaction if it has location attribute,
 * and it's geo location is inside Bounding Box.
 *
 * This will ignore altitude, e.g. it will select 3D transaction if it fits 2D BBox.
 * If transaction doesn't have location information, it will not be selected.
 *
 * @param south bbox bottom edge
 * @param west  bbox left edge
 * @param north bbox top edge
 * @param east  bbox right edge
 */
final case class TxnFilterBBoxLatLon(
  south: BigDecimal,
  west: BigDecimal,
  north: BigDecimal,
  east: BigDecimal
) extends BBoxLatLon {

  override def text(indent: String): Seq[String] = {
    val myIndent = indent + "  "

    Seq(
      indent + "Txn Bounding Box 2D",
      myIndent + "North, East: " + "geo:" + GeoPoint.frmt(north) + "," + GeoPoint.frmt(east),
      myIndent + "South, West: " + "geo:" + GeoPoint.frmt(south) + "," + GeoPoint.frmt(west),
    )
  }
}

/**
 * Select Transaction if it has location attribute,
 * and it's geo location is inside Bounding Box.
 *
 * This will select only transactions with altitude,
 * e.g. it will not select any 2D transaction, even if it fits 2D BBox.
 *
 * If transaction doesn't have location information, it will not be selected.
 *
 * @param south  bbox bottom edge
 * @param west   bbox left dege
 * @param depth  bbox floor
 * @param north  bbox top edge
 * @param east   bbox right edge
 * @param height bbox ceiling
 */
final case class TxnFilterBBoxLatLonAlt(
  south: BigDecimal,
  west: BigDecimal,
  depth: BigDecimal,
  north: BigDecimal,
  east: BigDecimal,
  height: BigDecimal
) extends BBoxLatLon {

  if (height < depth) {
    throw new IllegalArgumentException("Invalid Bounding Box: height is less than depth. " +
      "Depth: " + GeoPoint.frmt(depth) + "; Height: " + GeoPoint.frmt(height))
  }

  // height is tested by height < depth test
  if (depth < -6378137) {
    throw new IllegalArgumentException("Invalid Bounding Box: Depth is beyond center of Earth. " +
      "Depth: " + GeoPoint.frmt(depth))
  }

  override def text(indent: String): Seq[String] = {
    val myIndent = indent + "  "

    Seq(
      indent + "Txn Bounding Box 3D",
      myIndent + "North, East, Height: " + "geo:" + GeoPoint.frmt(north) + "," + GeoPoint.frmt(east) + "," + GeoPoint.frmt(height),
      myIndent + "South, West, Depth:  " + "geo:" + GeoPoint.frmt(south) + "," + GeoPoint.frmt(west) + "," + GeoPoint.frmt(depth)
    )
  }
}

/**
 * Select transaction if regular expression matches any of txn comments.
 *
 * Used regular expression engine is java.util.regex.Pattern.
 *
 * @param regex to match any of txn comments.
 */
final case class TxnFilterTxnComments(regex: String) extends TxnFilterRegex(regex) {
  val target = "Txn Comments"
}

/*
 *
 * TXN: POSTINGS
 *
 */

/**
 * Select transaction if regular expression matches
 * any of this transactions's posting accounts.
 *
 * Used regular expression engine is java.util.regex.Pattern.
 *
 * @param regex to match any of this transaction's posting accounts
 */
final case class TxnFilterPostingAccount(regex: String) extends TxnFilterRegex(regex) {
  val target = "Posting Account"

}

/**
 * Select transaction if regular expression matches
 * any of this transaction's posting comment.
 *
 * Used regular expression engine is java.util.regex.Pattern.
 *
 * @param regex to match any of this transaction's posting comment.
 */
final case class TxnFilterPostingComment(regex: String) extends TxnFilterRegex(regex) {
  val target = "Posting Comment"

}

sealed abstract class TxnFilterPosting(regex: String, amount: BigDecimal) extends TxnFilterRegex(regex) {
  val opTxt: String

  override def text(indent: String): Seq[String] = {
    val myIndent = indent + "  "

    Seq(
      indent + target,
      myIndent + "account: " + "\"" + s"${regex}" + "\"",
      myIndent + "amount " + opTxt + " " + amount.toString)
  }
}

/**
 * Select transaction if regular expression matches
 * any of this transaction's posting account
 * and at the same time amount of that posting is equal
 * with specified amount.
 *
 * Q: Why there is also account regex as parameter?
 * A: For consistency with less and greater, where it's mandatory.
 *
 * Used regular expression engine is java.util.regex.Pattern.
 *
 * @param regex to match any of this transaction's posting accounts
 * @param amount amount of that matched account (by regex) must be exactly this
 */
final case class TxnFilterPostingAmountEqual(regex: String, amount: BigDecimal) extends TxnFilterPosting(regex, amount) {
  val target = "Posting Amount"
  val opTxt: String = "=="
}


/**
 * Select transaction if regular expression matches
 * any of this transaction's posting account
 * and at the same time amount of that posting is less
 * than specified amount.
 *
 * Q: Why there is also account regex as parameter?
 * A: Sum of all postings inside transaction must be zero.
 *    If you select "less than some positive amount",
 *    be postings with negative amounts in every transaction
 *    to zero out the whole transaction.
 *
 * Used regular expression engine is java.util.regex.Pattern.
 *
 * @param regex to match any of this transaction's posting accounts
 * @param amount amount of matched account (by regex) must be less than this
 */
final case class TxnFilterPostingAmountLess(regex: String, amount: BigDecimal) extends TxnFilterPosting(regex, amount) {
  val target = "Posting Amount"
  val opTxt: String = "<"
}

/**
 * Select transaction if regular expression matches
 * any of this transaction's posting account
 * and at the same time amount of that posting is less
 * than specified amount.
 *
 * Q: Why there is also account regex as parameter?
 * A: Sum of all postings inside transaction must be zero.
 *    If you select "more than some negative amount",
 *    then all transactions will match, because there must
 *    be postings with positive amounts in every transaction
 *    to zero out the whole transaction.
 *
 * Used regular expression engine is java.util.regex.Pattern.
 *
 * @param regex to match any of this transaction's posting accounts
 * @param amount amount of matched account (by regex) must be greater than this
 */
final case class TxnFilterPostingAmountGreater(regex: String, amount: BigDecimal) extends TxnFilterPosting(regex, amount) {
  val target = "Posting Amount"
  val opTxt: String = ">"
}


/**
 * Select transaction if regular expression matches
 * any of this transaction's posting commodity.
 *
 * Used regular expression engine is java.util.regex.Pattern.
 *
 * @param regex to match posting commodity
 */
final case class TxnFilterPostingCommodity(regex: String) extends TxnFilterRegex(regex) {
  val target = "Posting Commodity"
}
