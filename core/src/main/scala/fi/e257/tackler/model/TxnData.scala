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

import fi.e257.tackler.api.{Checksum, Metadata, MetadataItem, TxnFilterDefinition, TxnFilterMetadata, TxnSetChecksum}
import fi.e257.tackler.core.{Hash, Settings, TacklerException}
import fi.e257.tackler.filter._

/**
 * Transaction data and associated metadata.
 *
 * @param metadata optional metadata about these transactions
 * @param txns     transactions
 */
class TxnData private (val metadata: Option[Metadata], val txns: Txns) {

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

    val hashName = metadata.fold[Option[String]](None) { md =>
        md.txnSetChecksum.fold[Option[String]](None) {
          tsc => Some(tsc.hash.algorithm)
        }
    }

    val filterInfo = Seq(TxnFilterMetadata(txnFilter))
    val mdis: Seq[MetadataItem] = metadata.map(_.metadataItems).getOrElse(Nil) ++ filterInfo

    val newtxns = txns.filter(txnFilter.filter)

    val newMD: Option[Metadata] = hashName.map(name => {
      Some(
        Metadata(
          Some(TxnSetChecksum(TxnData.calcTxnSetChecksum(newtxns, Hash(name)))),
          mdis))
    }).getOrElse(TxnData.getNewMetadata(mdis))

    new TxnData(newMD, newtxns)
  }
}

object TxnData {
  private def calcTxnSetChecksum(txns: Txns, hash: Hash): Checksum = {
    hash.checksum(
      txns
        .map(_.header.uuid match {
          case Some(uuid) => uuid.toString
          case None => throw new TacklerException("Missing txn uuid while calculating txn set checksum")
        })
        .sorted,
      "\n"
    )
  }

  private def getNewMetadata(mdis: Seq[MetadataItem]) = {
    if (mdis.isEmpty) {
      None
    } else {
      Some(Metadata(None, mdis))
    }
  }

  def apply(mdis: Seq[MetadataItem], txns: Txns, settingsOpt: Option[Settings]): TxnData = {

    val newMD = settingsOpt.map(settings => {
      if (settings.Auditing.txnSetChecksum) {
        Some(
          Metadata(
            Some(TxnSetChecksum(calcTxnSetChecksum(txns, settings.Auditing.hash))),
            mdis))
      } else {
        getNewMetadata(mdis)
      }
    }).getOrElse(getNewMetadata(mdis))

    new TxnData(newMD, txns)
  }
}