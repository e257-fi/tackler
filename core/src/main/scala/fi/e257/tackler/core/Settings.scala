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

import java.nio.file.Path
import java.time.{LocalTime, ZoneId}

import better.files._
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import fi.e257.tackler.model.AccountTreeNode

/**
 * Config keys / paths. All of these keys / paths
 * must be available (by embedded default conf).
 */
object CfgKeys {
  val timezone: String = "timezone"

  val basedir: String = "basedir"

  object Auditing {
    val keybase: String = "auditing"

    val hash: String = keybase + "." + "hash"

    val txnSetChecksum: String = keybase + "." + "txn-set-checksum"
  }


  val input_storage: String = "input.storage"

  val input_git_repository: String = "input.git.repository"
  val input_git_ref: String = "input.git.ref"
  val input_git_dir: String = "input.git.dir"
  val input_git_suffix: String = "input.git.suffix"

  val input_fs_dir: String = "input.fs.dir"
  val input_fs_glob: String = "input.fs.glob"

  object Accounts {
    val keybase: String = "accounts"

    val strict: String      = keybase + "." + "strict"
    val coa: String         = keybase + "." + "coa"
    val commodities: String = keybase + "." + "commodities"
    val permit_empty_commodity: String = keybase + "." + "permit-empty-commodity"
  }

  val reporting: String = "reporting"

  val reporting_reports: String  = "reporting.reports"
  val reporting_exports: String  = "reporting.exports"

  val reporting_formats: String  = "reporting.formats"

  val reporting_scale_min: String  = "reporting.scale.min"
  val reporting_scale_max: String  = "reporting.scale.max"

  val reporting_accounts: String = "reporting.accounts"
  val reporting_console: String = "reporting.console"

  object Reports {
    protected val keybase: String = "reports"

    object Balance {
      val keybase: String = Reports.keybase + "." + "balance"

      val minScale: String = keybase + "." + "scale.min"
      val maxScale: String = keybase + "." + "scale.max"

      val title: String = keybase + "." + "title"
      val accounts: String = keybase + "." + "accounts"
    }

    object BalanceGroup {
      val keybase: String = Reports.keybase + "." + "balance-group"

      val minScale: String = keybase + "." + "scale.min"
      val maxScale: String = keybase + "." + "scale.max"

      val title: String = keybase + "." + "title"
      val accounts: String = keybase + "." + "accounts"
      val groupBy: String = keybase + "." + "group-by"
    }

    object Register {
      val keybase: String = Reports.keybase + "." + "register"

      val minScale: String = keybase + "." + "scale.min"
      val maxScale: String = keybase + "." + "scale.max"

      val title: String = keybase + "." + "title"
      val accounts: String = keybase + "." + "accounts"
    }
  }

  object Exports {
    protected val keybase: String = "exports"

    object Equity {
      protected val keybase: String = Exports.keybase + "." + "equity"

      // Export: => no title
      val accounts: String = keybase + "." + "accounts"
    }
  }
}

/**
 * Different selections which are possible to be made by CLI or CONF-file
 */
object Settings {
  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  val balance = "balance"
  val balanceGroup = "balance-group"
  val register = "register"

  val equity = "equity"
  val identity = "identity"

  val json = "json"
  val txt = "txt"

  val year = "year"
  val month = "month"
  val date = "date"
  val isoWeek = "iso-week"
  val isoWeekDate = "iso-week-date"

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def apply(cfgPath: Path, initialConfig: Config): Settings = {

    File(cfgPath).verifiedExists(File.LinkOptions.follow) match {
      case Some(true) => new Settings(Some(cfgPath), initialConfig)
      case _ => {
        log.error("Configuration file is not found or it is not readable: [" + cfgPath.toString + "]")
        log.warn("Settings will NOT use configuration file, only provided and embedded configuration will be used")
        new Settings(None, initialConfig)
      }
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def apply(config: Config): Settings = {
    new Settings(None, config)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def apply(): Settings = {
    new Settings(None, ConfigFactory.empty())
  }
}

class Settings(optPath: Option[Path], providedConfig: Config) {

  private val cfgBasename = "tackler.core"

  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  val cfg: Config = optPath match {
    case Some(path) => {
      log.info("loading configuration with cfg-file: " + path.toString)

      providedConfig
        .withFallback(ConfigFactory.parseFile(path.toFile).getConfig(cfgBasename))
        .withFallback(ConfigFactory.load().getConfig(cfgBasename))
        .resolve()
    }
    case None => {
      log.debug("Loading plain configuration")

      providedConfig
        .withFallback(ConfigFactory.load().getConfig(cfgBasename))
        .resolve()
    }
  }

  /**
   * Default timezone to be used in case of missing ZoneId / Offset
   */
  val timezone: ZoneId = ZoneId.of(cfg.getString(CfgKeys.timezone))

  /**
   * Default time to be used if time component is missing from Txn
   * Far-Far-Away: defaultTime could be set by conf
   */
  val defaultTime: LocalTime = LocalTime.MIN

  val basedir: Path = optPath.fold(
    File(cfg.getString(CfgKeys.basedir)).path
  )(path => getPathWithAnchor(cfg.getString(CfgKeys.basedir), path))

  object Auditing {
    val hash: Hash = Hash(cfg.getString(CfgKeys.Auditing.hash))

    val txnSetChecksum: Boolean = cfg.getBoolean(CfgKeys.Auditing.txnSetChecksum)
  }

  val input_storage: StorageType = StorageType(cfg.getString(CfgKeys.input_storage))

  val input_git_repository: Path =
    getPathWithAnchor(cfg.getString(CfgKeys.input_git_repository), basedir)
  val input_git_ref: String = cfg.getString(CfgKeys.input_git_ref)
  val input_git_dir: String = cfg.getString(CfgKeys.input_git_dir)
  val input_git_suffix: String = cfg.getString(CfgKeys.input_git_suffix)


  val input_fs_dir: Path =
    getPathWithAnchor(cfg.getString(CfgKeys.input_fs_dir), basedir)

  val input_fs_glob: String = cfg.getString(CfgKeys.input_fs_glob)


  object Accounts {
    val strict: Boolean = cfg.getBoolean(CfgKeys.Accounts.strict)

    val coa: Map[String, AccountTreeNode] = cfg.getStringList(CfgKeys.Accounts.coa).asScala
      .toSet[String].map(acc => (acc, AccountTreeNode(acc, None))).toMap

    val commodities: Set[String] = cfg.getStringList(CfgKeys.Accounts.commodities).asScala.toSet[String]

    val permit_empty_commodity: Boolean = cfg.getBoolean(CfgKeys.Accounts.permit_empty_commodity)
  }

  /**
   * Reporting
   */
  object Reporting {
    val reports: List[ReportType] = cfg.getStringList(CfgKeys.reporting_reports).asScala
      .map(ReportType(_)).toList

    val exports: List[ExportType] = cfg.getStringList(CfgKeys.reporting_exports).asScala
      .map(ExportType(_)).toList

    val formats: List[ReportFormat] = cfg.getStringList(CfgKeys.reporting_formats).asScala
      .map(ReportFormat(_)).toList

    val accounts: List[String] = cfg.getStringList(CfgKeys.reporting_accounts).asScala.toList

    val console: Boolean = cfg.getBoolean(CfgKeys.reporting_console)
  }

  object Reports {
    object Balance {
      protected val keys = CfgKeys.Reports.Balance
      private val scale = getReportScale(keys.keybase)
      val minScale: Int = scale._1
      val maxScale: Int = scale._2

      val title: String = cfg.getString(keys.title)
      val accounts: List[String] = getReportAccounts(keys.accounts)
    }

    object BalanceGroup {
      protected val keys = CfgKeys.Reports.BalanceGroup

      private val scale = getReportScale(keys.keybase)

      val minScale: Int = scale._1
      val maxScale: Int = scale._2

      val title: String = cfg.getString(keys.title)
      val accounts: List[String] = getReportAccounts(keys.accounts)
      // todo: this is lazy evaluated?
      // to trigger, remove output from
      // tests/reporting/ex/GroupByException-unknown-group-by.exec
      // test:uuid: 31e0bd80-d4a9-4d93-915d-fa2424aedb84
      val groupBy: GroupBy = GroupBy(cfg.getString(keys.groupBy))
    }

    object Register {
      protected val keys = CfgKeys.Reports.Register

      private val scale = getReportScale(keys.keybase)
      val minScale: Int = scale._1
      val maxScale: Int = scale._2

      val title: String = cfg.getString(keys.title)
      val accounts: List[String] = getReportAccounts(keys.accounts)
    }
  }

  object Exports {
    object Equity {
      protected val keys = CfgKeys.Exports.Equity

      val accounts: List[String] = getReportAccounts(keys.accounts)
    }
  }

  def getReportScale(keyBase: String): (Int, Int) = {

    def getScale(keyBase: String, key: String): Int = {
      val fullKey = keyBase + "." + key

      val k = if (cfg.hasPath(fullKey)) {
        fullKey
      } else {
        CfgKeys.reporting + "." + key
      }

      val scale = cfg.getInt(k)

      if (scale < 0){
        val msg = "scale can not be negative. CFG key: " + k
        log.error(msg)
        throw new ConfigurationException(msg)
      }

      scale
    }

    val minScaleKey = "scale.min"
    val maxScaleKey = "scale.max"

    val minScale = getScale(keyBase, minScaleKey)
    val maxScale = getScale(keyBase, maxScaleKey)

    if (maxScale < minScale) {
      val msg = "max scale can not be smaller than min scale. CFG keys: " + CfgKeys.reporting + ", and/or keys: " + keyBase
      log.error(msg)
      log.error("max scale: {}, min scale: {}", maxScale, minScale)
      throw new ConfigurationException(msg)
    }

    (minScale, maxScale)
  }

  def getReportAccounts(key: String): List[String] = {
    if (cfg.hasPath(key)) {
      cfg.getStringList(key).asScala.toList
    } else {
      this.Reporting.accounts
    }
  }

  /**
   * Translates string to path.
   * If param path as string is not absolute, returns
   * absolute path based on basedir.
   *
   * If param path is absolute, then just translates it
   * to canonical Path.
   *
   * @param path as string
   * @return abs Path
   */
  def getPathWithSettings(path: String): Path = {
    getPathWithAnchor(path, basedir)
  }

  /**
   * Get path with anchor (helper for Better-files).
   *
   * @param path relative or absolute
   * @param anchor used as anchor if path is not absolute
   * @return resulting absolute path
   */
  private def getPathWithAnchor(path: String, anchor: Path): Path = {
    File(File(anchor), path).path
  }
}
