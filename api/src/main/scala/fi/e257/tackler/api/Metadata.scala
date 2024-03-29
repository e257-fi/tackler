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
package fi.e257.tackler.api

import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

import io.circe._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

sealed trait MetadataItem {
  def text(): Seq[String]
}

object MetadataItem {
  @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
  implicit val decodeMetadataItem: Decoder[MetadataItem] = deriveDecoder[MetadataItem]

  @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
  implicit val encodeMetadataItem: Encoder[MetadataItem] = deriveEncoder[MetadataItem]
}


final case class Metadata(items: Seq[MetadataItem]) {

  def mkString(start: String, sep: String, end: String): String = {
    items.flatMap(_.text() ++ Seq("")).mkString(start, sep, end)
  }

  def text(): String = {
    mkString("", "\n", "\n")
  }

  def ++(mdis: Seq[MetadataItem]): Metadata = {
    Metadata(items ++ mdis)
  }
}
object Metadata {
  def append(md: Option[Metadata], item: Option[MetadataItem]): Option[Metadata] = {
    md.fold(item.fold[Option[Metadata]](None) { mdi =>
      Some(Metadata(Seq(mdi)))
    }) { m =>
      item.fold[Option[Metadata]](Some(m)) { i =>
        Some(m ++ Seq(i))
      }
    }
  }

  implicit val decodeMetadata: Decoder[Metadata] = deriveDecoder[Metadata]
  implicit val encodeMetadata: Encoder[Metadata] = deriveEncoder[Metadata]
}


final case class Checksum(algorithm: String, value: String)

object Checksum {
  @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
  implicit val decodeHash: Decoder[Checksum] = deriveDecoder[Checksum]

  @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
  implicit val encodeHash: Encoder[Checksum] = deriveEncoder[Checksum]
}


/**
 * Txn Set Checksum metadata item
 *
 * @param size of transaction set
 * @param hash of Txn Set Checksum
 */
final case class TxnSetChecksum(size: Int, hash: Checksum) extends MetadataItem {
  override def text(): Seq[String] = {
    Seq(
      "Txn set checksum:",
      "  " + hash.algorithm + ": " + hash.value,
      "  " + "Set size: %d".format(size)
    )
  }
}


/**
 * Account Selector Checksum metadata item
 *
 * @param hash of account selector
 */
final case class AccountSelectorChecksum(hash: Checksum) extends MetadataItem {
  override def text(): Seq[String] = {
    Seq(
      "Account selector checksum:",
      "  " + hash.algorithm + ": " + hash.value)
  }
}

/**
 * Information about report's timezone
 */
final case class TimeZoneInfo(zoneId: ZoneId) extends MetadataItem {
  override def text(): Seq[String] = {
    Seq(
      "Active time zone:",
      "  " + "TZ name: " + zoneId.getDisplayName(TextStyle.NARROW, Locale.getDefault),
    )
  }
}

object TimeZoneInfo {
  /**
   * Build optional TimeZoneInfo item
   *
   * @param zoneId optional zoneId
   * @return Optional TimeZoneInfo
   */
  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def apply(zoneId: Option[ZoneId]): Option[TimeZoneInfo] = {
    zoneId.map(tz => TimeZoneInfo(tz))
  }
}

/**
 * Description of used Txn Filters
 *
 * @param txnFilterDef is Filter definition used to filter transactions
 */
final case class TxnFilterDescription(txnFilterDef: TxnFilterDefinition) extends MetadataItem {
  override def text(): Seq[String] = txnFilterDef.text("")
}


/**
 * Trait for txn input related metadata items.
 */
sealed trait InputMetadataItem extends MetadataItem


/**
 * Reference information for Git txn input
 *
 * @param commit  is commit id (sha1) of used git tree
 * @param ref     is set if source selection  was done by git reference
 * @param dir     is path of top level directory which contains txns inside repository
 * @param suffix  is used to select txn
 * @param message is short message of commit (one-line format)
 */
final case class GitInputReference(commit: String, ref: Option[String], dir: String, suffix: String, message: String)
  extends InputMetadataItem {

  override def text(): Seq[String] = {
    Seq(
      "Git storage:",
      "  commit:  " + commit,
      "  ref:     " + ref.getOrElse("FIXED by commit"),
      "  dir:     " + dir,
      "  suffix:  " + suffix,
      "  message: " + message,
    )
  }
}
