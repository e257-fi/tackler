/*
 * Copyright 2016-2021 E257.FI
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

import better.files._
import cats.syntax.all._
import com.typesafe.config.{Config, ConfigFactory}
import fi.e257.tackler.model.AccountTreeNode
import org.slf4j.{Logger, LoggerFactory}

import java.nio.file.Path
import java.time.{LocalTime, ZoneId}
import scala.jdk.CollectionConverters._

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
    val deprecated_coa: String  = keybase + "." + "coa"
    val coa: String         = keybase + "." + "chart-of-accounts"
    val commodities: String = keybase + "." + "commodities"
    val permit_empty_commodity: String = keybase + "." + "permit-empty-commodity"
  }

  object Tags {
    val keybase: String = "tags"

    val strict: String      = keybase + "." + "strict"
    val cot: String         = keybase + "." + "chart-of-tags"
  }

  val reporting: String = "reporting"

  val reporting_reportTZ: String = "reporting.report-timezone"

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

      val tsStyle: String = keybase + "." + "timestamp-style"
      val title: String = keybase + "." + "title"
      val accounts: String = keybase + "." + "accounts"
    }
  }

  object Exports {
    protected val keybase: String = "exports"

    object Equity {
      // Export: => no title, no scale
      protected val keybase: String = Exports.keybase + "." + "equity"

      val equityAccount: String = keybase + "." + "equity-account"

      val accounts: String = keybase + "." + "accounts"
    }
  }
}

object CfgValues {
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

  val tsDateStyle = "date"
  val tsSecondsStyle = "seconds"
  val tsFullStyle = "full"
}

/**
 * Different selections which are possible to be made by CLI or CONF-file
 */
object Settings {
  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  private def fnlz(s: Settings): Settings = {
    // Force initialization of all inner objects of Setting
    // This will trigger configuration errors immediately and not at the time of usage
    s.Auditing
    s.Accounts
    s.Tags
    s.Reporting
    s.Reports
    s.Reports.Balance
    s.Reports.BalanceGroup
    s.Reports.Register
    s.Exports
    s.Exports.Equity
    s
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def apply(cfgPath: Path, initialConfig: Config): Settings = {

    val optPath = File(cfgPath).verifiedExists(File.LinkOptions.follow)
    val s = optPath match {
      case Some(true) => {
        try {
          new Settings(Some(cfgPath), initialConfig)
        } catch {
          case ex: com.typesafe.config.ConfigException => {
            val msg = "CFG: error: " + ex.getMessage
            log.error(msg)
            throw  new ConfigurationException(msg)
          }
        }
      }
      case _ => {
        log.error("CFG: Configuration file is not found or it is not readable: [" + cfgPath.toString + "]")
        log.warn("CFG: Settings will NOT use configuration file, only provided and embedded configuration will be used")
        new Settings(None, initialConfig)
      }
    }
    fnlz(s)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def apply(config: Config): Settings = {
    fnlz(new Settings(None, config))
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def apply(): Settings = {
    fnlz(new Settings(None, ConfigFactory.empty()))
  }
}

class Settings(optPath: Option[Path], providedConfig: Config) {

  private val cfgBasename = "tackler.core"

  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  val cfg: Config = optPath match {
    case Some(path) => {
      log.info("CFG: using file: {}", path.toString)

      providedConfig
        .withFallback(ConfigFactory.parseFile(path.toFile).getConfig(cfgBasename))
        .withFallback(ConfigFactory.load().getConfig(cfgBasename))
        .resolve()
    }
    case None => {
      log.info("CFG: Loading embedded, default configuration")

      providedConfig
        .withFallback(ConfigFactory.load().getConfig(cfgBasename))
        .resolve()
    }
  }

  /**
   * Default timezone to be used in case of missing ZoneId / Offset
   */
  val timezone: ZoneId = getTimezoneEx(CfgKeys.timezone)

  /**
   * Default time to be used if time component is missing from Txn
   * Far-Far-Away: defaultTime could be set by conf
   */
  val defaultTime: LocalTime = LocalTime.MIN

  val basedir: Path = optPath.fold(
    File(cfg.getString(CfgKeys.basedir)).path
  )(path => getPathWithAnchor(cfg.getString(CfgKeys.basedir), path))

  object Auditing {
    val hash: Hash = getHash(CfgKeys.Auditing.hash)

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

    val coa: Map[String, AccountTreeNode] = getChartOfAccounts(CfgKeys.Accounts.coa, CfgKeys.Accounts.deprecated_coa)

    val commodities: Set[String] = cfg.getStringList(CfgKeys.Accounts.commodities).asScala.toSet[String]

    val permit_empty_commodity: Boolean = cfg.getBoolean(CfgKeys.Accounts.permit_empty_commodity)
  }

  object Tags {
    val strict: Boolean = cfg.getBoolean(CfgKeys.Tags.strict)

    val cot: List[String] = cfg.getStringList(CfgKeys.Tags.cot).asScala.toList
  }

  /**
   * Reporting
   */
  object Reporting {

    val reportTZ: Option[ZoneId]= getTimezone(CfgKeys.reporting_reportTZ)

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
      // test: 31e0bd80-d4a9-4d93-915d-fa2424aedb84
      // exec: tests/reporting/ex/GroupByException-unknown-group-by.exec
      val groupBy: GroupBy = GroupBy(cfg.getString(keys.groupBy))
    }

    object Register {
      protected val keys = CfgKeys.Reports.Register

      private val scale = getReportScale(keys.keybase)

      val minScale: Int = scale._1
      val maxScale: Int = scale._2

      val tsStyle: TimestampStyle = getTimestampStyle(keys.tsStyle)
      val title: String = cfg.getString(keys.title)
      val accounts: List[String] = getReportAccounts(keys.accounts)
    }
  }

  object Exports {
    object Equity {
      protected val keys = CfgKeys.Exports.Equity

      val equityAccount:String = getEquityAccount(keys.equityAccount)

      val accounts: List[String] = getReportAccounts(keys.accounts)
    }
  }

  /**
   * Get Chart of Accounts
   *
   * Get CoA by either new key or deprecated old key. If both are defined, throw up
   * @param key current settings key
   * @param deprecatedKey deprecated key
   * @return chart of account, if none of keys is defined, then it's empty
   */
  def getChartOfAccounts(key: String, deprecatedKey: String): Map[String, AccountTreeNode] = {
    val accounts = if (cfg.hasPath(key)) {
      if (cfg.hasPath(deprecatedKey)) {
        val msg = s"Chart of Accounts has both current key '$key' and deprecated key '$deprecatedKey' defined"
        log.error(msg)
        throw new ConfigurationException(msg)
      }
      cfg.getStringList(key).asScala
    } else if (cfg.hasPath(deprecatedKey)){
      log.warn(s"Using deprecated settings key ($deprecatedKey) for Chart of Accounts")
      log.warn(s"Chart of Accounts should be defined by $key")
      cfg.getStringList(deprecatedKey).asScala

    } else {
      List.empty[String]
    }
    accounts.toSet[String].map(acc => (acc, AccountTreeNode(acc, None))).toMap
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
        val msg = "CFG: scale can not be negative: conf-key: " + k
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
      val msg = "CFG: max scale can not be smaller than min scale. conf keys: " + CfgKeys.reporting + ", and/or keys: " + keyBase
      log.error(msg)
      log.error("CFG: max scale: {}, min scale: {}", maxScale, minScale)
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

  def getEquityAccount(key: String): String = {
    if (cfg.hasPath(key)) {
      val eqAccount = cfg.getString(key)
      if (Accounts.strict) {
        if (!Accounts.coa.exists({ case (key, _) => key === eqAccount })) {
          val msg = s"CFG: Unknown account [${eqAccount}] for Equity Account, and strict mode is activated"
          log.error(msg)
          throw new ConfigurationException(msg)
        }
      }
      eqAccount
    } else {
      "Equity:Balance"
    }
  }

  def getTimezoneEx(key: String): ZoneId = {
    try {
      ZoneId.of(cfg.getString(key))
    } catch {
      case ex @(_ :java.time.zone.ZoneRulesException | _ : java.time.DateTimeException) =>
        val msg = "CFG: Invalid time zone for '" + key + "'. Error was: " + ex.getMessage
        log.error(msg)
        throw new ConfigurationException(msg)
    }
  }

  /**
   * Get optional timezone
   *
   * @param key
   * @return Some(zoneId) or None
   */
  def getTimezone(key: String): Option[ZoneId] = {
    if (cfg.hasPath(key)) {
      Some(getTimezoneEx(key))
    } else {
      None
    }
  }

  /**
   * Get timestamp style for reports
   *
   * Default is date
   *
   * @param key
   * @return timestamp style
   */
  def getTimestampStyle(key: String): TimestampStyle = {
    if (cfg.hasPath(key))
      TimestampStyle(cfg.getString(key))
    else {
      DateTsStyle()
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

  private def getHash(key: String): Hash = {
    try {
      Hash(cfg.getString(CfgKeys.Auditing.hash))
    } catch {
      case ex: java.security.NoSuchAlgorithmException =>
      val msg = "CFG: Invalid algorithm for '" + key + "'. Error was: " + ex.getMessage
      log.error(msg)
      throw new ConfigurationException(msg)
    }
  }
}
