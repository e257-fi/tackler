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
package fi.e257.tackler.model

import fi.e257.tackler.api._
import fi.e257.tackler.core.{AccountSelector, Hash, Settings, TxnException}
import fi.e257.tackler.filter._
import cats.implicits._

/**
 * Transaction data and associated metadata.
 *
 * @param metadata optional metadata about these transactions
 * @param txns     transactions
 */
class TxnData private (val metadata: Option[Metadata], val txns: Txns, val algorithm: Option[String]) {

  override def toString: String = {
    metadata.fold("")(_.text())
  }

  def getMetadata(accounts: AccountSelector): Option[Metadata] = {
    metadata.map(md => {
      if (algorithm.isDefined) {
        md ++ Seq(AccountSelectorChecksum(accounts.checksum()))
      } else {
        md
      }
    })
  }

  /**
   * Filter this TxnData based on provided transaction filter.
   * Resulting txn sequence will contain only those transaction
   * which are selected by filter.
   *
   * @param txnFilter is transaction filter definition
   * @return new [[TxnData]] which contains filtered txn sequence.
   *         Metadata of returned [[TxnData]] is augmented with TxnFilterMetadata item
   *         which contains information about used filter.
   */
  def filter(txnFilter: TxnFilterDefinition): TxnData = {

    val filterInfo = Seq(TxnFilterMetadata(txnFilter))

    val newTxns = txns.filter(txnFilter.filter)

    val newTSC = algorithm.map(name => {
      TxnSetChecksum(
        newTxns.size,
        TxnData.calcTxnSetChecksum(newTxns, Hash(name)))
    }).toList

    val noTSC: Seq[MetadataItem] = metadata.fold(Seq.empty[MetadataItem])(md => {
      md.metadataItems.filter {
        case _: TxnSetChecksum => false
        case _ => true
      }
    })

    val newMD = Some(Metadata(newTSC ++ noTSC ++ filterInfo))

    new TxnData(newMD, newTxns, algorithm)
  }
}

object TxnData {
  private def calcTxnSetChecksum(txns: Txns, hash: Hash): Checksum = {
    val uuids = txns
      .map(_.header.uuid match {
        case Some(uuid) => uuid.toString
        case None => throw new TxnException("Found missing txn uuid with txn set checksum")
      })
      .sorted

    val dup = uuids
      .foldLeft(("", "", 0)) { case (runner, uuid) =>
        if (runner._1 === uuid) {
          (uuid, uuid, runner._3 + 1)
        } else {
          (uuid, runner._2, runner._3)
        }
      }

    if (dup._3 =!= 0) {
      val msg =
        s"""Found ${dup._3 + 1} duplicate txn uuids with txn set checksum. """ +
        s"""At least "${dup._2}" is duplicate, there could be others."""
      throw new TxnException(msg)
    }

    hash.checksum(uuids, "\n")
  }

  private def getNewMetadata(mdis: Seq[MetadataItem]) = {
    if (mdis.isEmpty) {
      None
    } else {
      Some(Metadata(mdis))
    }
  }

  def apply(mdis: Seq[MetadataItem], txns: Txns, settingsOpt: Option[Settings]): TxnData = {

    val auditHash = settingsOpt.fold[Option[Hash]](None)(settings => {
      if (settings.Auditing.txnSetChecksum) {
        Some(settings.Auditing.hash)
      } else {
        None
      }
    })

    val newMD = auditHash match {
      case Some(hash) => {
        val tsc = Seq(TxnSetChecksum(txns.size, calcTxnSetChecksum(txns, hash)))
        Some(Metadata(tsc ++ mdis))
      }
      case None =>
        getNewMetadata(mdis)
    }

    new TxnData(newMD, txns, auditHash.map(_.algorithm))
  }
}
