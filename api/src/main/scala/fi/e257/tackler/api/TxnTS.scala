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

import java.time.{ZoneId, ZonedDateTime}
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder, SignStyle}
import java.time.temporal.ChronoField.DAY_OF_WEEK
import java.time.temporal.{ChronoField, IsoFields}

object TxnTS {

  private def zonedFormatter(formatter: DateTimeFormatterBuilder, zoneSpace: String) = {
    formatter
      .appendLiteral(zoneSpace)
      .appendOffset("+HH:MM", "Z")
      .toFormatter
  }

  private def secondsPartial(): DateTimeFormatterBuilder = {
    new DateTimeFormatterBuilder()
      .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
      .appendLiteral('-').appendValue(ChronoField.MONTH_OF_YEAR, 2)
      .appendLiteral('-').appendValue(ChronoField.DAY_OF_MONTH, 2)
      .appendLiteral(" ")
      .appendValue(ChronoField.HOUR_OF_DAY, 2)
      .appendLiteral(':').appendValue(ChronoField.MINUTE_OF_HOUR, 2)
      .appendLiteral(':').appendValue(ChronoField.SECOND_OF_MINUTE, 2)
  }

  private def localSecondsFormatter() = {
    secondsPartial().toFormatter
  }

  private def fullPartial() = {
    secondsPartial()
      .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
  }

  private def localFullFormatter() = {
    fullPartial().toFormatter()
  }

  private val frmtZonedSeconds = zonedFormatter(secondsPartial(), " ")
  private val frmtLocalSeconds = localSecondsFormatter()

  private val frmtZonedFullTs = zonedFormatter(fullPartial(), " ")
  private val frmtLocalFullTs = localFullFormatter()


  private def weekPartial() = {
    new DateTimeFormatterBuilder()
      .appendValue(IsoFields.WEEK_BASED_YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
      .appendLiteral("-W")
      .appendValue(IsoFields.WEEK_OF_WEEK_BASED_YEAR, 2)
  }

  // no zoneId as with ISO_WEEK_DATE
  // no localized day number as with 'e' (e.g. en_US => sunday == 1)
  private def weekDatePartial() = {
    weekPartial()
      .appendLiteral('-')
      .appendValue(DAY_OF_WEEK, 1)
  }

  private def localWeekFormatter() = {
    weekPartial().toFormatter
  }

  private def localWeekDateFormatter() = {
    weekDatePartial().toFormatter
  }

  private val frmtISOWeek = zonedFormatter(weekPartial(), "")
  private val frmtISOWeekDate = zonedFormatter(weekDatePartial(), "")

  private val frmtTzWeek  = zonedFormatter(weekPartial(), " ")
  private val frmtTzWeekDate  = zonedFormatter(weekDatePartial(), " ")

  private val frmtLocalWeek = localWeekFormatter()
  private val frmtLocalWeekDate = localWeekDateFormatter()

  /**
   * ISO-8601 Timestamp with offset.
   *
   * @param ts timestamp
   * @return ISO-8601 date-time: 2016-12-17T12:31:12+03:00
   */
  def isoZonedTS(ts: ZonedDateTime): String = {
    ts.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
  }

  /**
   * ISO-8601 date with offset.
   *
   * @param ts timestamp
   * @return ISO-8601 date: 2016-12-17T12:31:12+03:00 => 2016-12-17+03:00
   */
  def isoDate(ts: ZonedDateTime): String = {
    ts.format(DateTimeFormatter.ISO_OFFSET_DATE)
  }

  /**
   * ISO-8601 month with offset.
   *
   * @param ts timestamp
   * @return ISO-8601 date: 2016-12-17T12:31:12+03:00 => 2016-12+03:00
   */
  def isoMonth(ts: ZonedDateTime): String = {
    ts.format(DateTimeFormatter.ofPattern("yyyy'-'MMXXX"))
  }

  /**
   * ISO-8601 year with offset.
   *
   * @param ts timestamp
   * @return ISO-8601 date: 2016-12-17T12:31:12+03:00 => 2016+03:00
   */
  def isoYear(ts: ZonedDateTime): String = {
    ts.format(DateTimeFormatter.ofPattern("yyyyXXX"))
  }

  /**
   * ISO-8601 Week with offset.
   *
   * @param ts timestamp
   * @return ISO-8601 week (without date): 2010-01-01 => 2009-W53+03:00
   */
  def isoWeek(ts: ZonedDateTime): String = {
    ts.format(frmtISOWeek)
  }

  /**
   * ISO-8601 Week date with offset.
   *
   * @param ts timestamp
   * @return ISO-8601 week date: 2010-01-01 => 2009-W53-5+03:00
   */
  def isoWeekDate(ts: ZonedDateTime): String = {
    ts.format(frmtISOWeekDate)
  }

  /**
   * Zoned timestamp (seconds)
   *
   * @param ts timestamp
   * @return date time (s) with offset: 2016-12-17 12:31:12 +03:00
   */
  def tzSeconds(ts: ZonedDateTime): String = {
    ts.format(frmtZonedSeconds)
  }

  /**
   * Zoned timestamp (nanoseconds)
   *
   * @param ts timestamp
   * @return date time (ns) with offset: 2016-12-17 12:31:12.123456789 +03:00
   */
  def tzFull(ts: ZonedDateTime): String = {
    ts.format(frmtZonedFullTs)
  }

  /**
   * Zoned timestamp (date)
   *
   * @param ts timestamp
   * @return  date with offset: 2016-12-17 +03:00
   */
  def tzDate(ts: ZonedDateTime): String = {
    ts.format(DateTimeFormatter.ofPattern("yyyy-MM-dd XXX"))
  }

  /**
   * Zoned timestamp (month)
   *
   * @param ts timestamp
   * @return year-month with offset: 2016-12 +03:00
   */
  def tzMonth(ts: ZonedDateTime): String = {
    ts.format(DateTimeFormatter.ofPattern("yyyy'-'MM XXX"))
  }

  /**
   * Zoned timestamp (year)
   *
   * @param ts timestamp
   * @return year with offset: 2016 +03:00
   */
  def tzYear(ts: ZonedDateTime): String = {
    ts.format(DateTimeFormatter.ofPattern("yyyy XXX"))
  }

  /**
   * Zoned timestamp (year-week with ISO-8601 rules)
   *
   * @param ts timestamp
   * @return year-week with offset: 2009-W53 +03:00
   */
  def tzWeek(ts: ZonedDateTime): String = {
    ts.format(frmtTzWeek)
  }

  /**
   * Zoned timestamp (year-week-day with ISO-8601 rules)
   *
   * @param ts timestamp
   * @return year-week-date and offset: 2009-W53-5 +03:00
   */
  def tzWeekDate(ts: ZonedDateTime): String = {
    ts.format(frmtTzWeekDate)
  }

  /**
   * Local timestamp (seconds)
   *
   * @param ts timestamp
   * @param localTZ local time zone
   * @return local ts (seconds): 2016-12-17 12:31:12
   */
  def localSeconds(ts: ZonedDateTime, localTZ: ZoneId): String = {
    ts.withZoneSameInstant(localTZ).format(frmtLocalSeconds)
  }

  /**
   * Local timestamp (nanoseconds)
   *
   * @param ts timestamp
   * @param localTZ local time zone
   * @return local ts (nanoseconds): 2016-12-17 12:31:12.123456789
   */
  def localFull(ts: ZonedDateTime, localTZ: ZoneId): String = {
    ts.withZoneSameInstant(localTZ).format(frmtLocalFullTs)
  }

  /**
   * Local timestamp (date)
   *
   * @param ts timestamp
   * @param localTZ local time zone
   * @return local ts (date): 2016-12-17
   */
  def localDate(ts: ZonedDateTime, localTZ: ZoneId): String = {
    ts.withZoneSameInstant(localTZ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
  }

  /**
   * Local timestamp (month)
   *
   * @param ts timestamp
   * @param localTZ local time zone
   * @return local ts (months): 2016-12
   */
  def localMonth(ts: ZonedDateTime, localTZ: ZoneId): String = {
    ts.withZoneSameInstant(localTZ).format(DateTimeFormatter.ofPattern("yyyy'-'MM"))
  }

  /**
   * Local timestamp (year)
   *
   * @param ts timestamp
   * @param localTZ local time zone
   * @return Local ts (year): 2016
   */
  def localYear(ts: ZonedDateTime, localTZ: ZoneId): String = {
    ts.withZoneSameInstant(localTZ).format(DateTimeFormatter.ofPattern("yyyy"))
  }

  /**
   *  Local timestamp (year-week with ISO-8601 rules)
   *
   * @param ts timestamp
   * @param localTZ local time zone
   * @return local year-week: 2009-W53
   */
  def localWeek(ts: ZonedDateTime, localTZ: ZoneId): String = {
    ts.withZoneSameInstant(localTZ).format(frmtLocalWeek)
  }

  /**
   * Local timestamp (week-date with ISO-8601 rules)
   *
   * @param ts timestamp
   * @param localTZ local time zone
   * @return local week date: 2009-W53-5
   */
  def localWeekDate(ts: ZonedDateTime, localTZ: ZoneId): String = {
    ts.withZoneSameInstant(localTZ).format(frmtLocalWeekDate)
  }


  object Shard {
    private def toUTC(ts: ZonedDateTime): ZonedDateTime = ts.withZoneSameInstant(ZoneId.of("UTC"))
    /**
     * Format of ISO-8601 week based shard
     *
     * Following is localized week date, e.g.(LANG=en_US.UTF-8) causes weeks to start on sunday=1
     *    val frmtISOWeek = DateTimeFormatter.ofPattern("YYYY'/W'ww")
     *
     * Following is with timezone
     *    val frmtISOWeek = DateTimeFormatter.ISO_WEEK_DATE
     *
     * So let's build it explicitly
     */
    private val shardISOWeekFormat = new DateTimeFormatterBuilder()
      .appendValue(IsoFields.WEEK_BASED_YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
      .appendLiteral("/W")
      .appendValue(IsoFields.WEEK_OF_WEEK_BASED_YEAR, 2)
      .toFormatter

    /**
     * Format of ISO-8601 week date based shard
     *
     * This is actual date (2009/W53/5) vs. week day '5'
     */
    private val shardISOWeekDayFormat = new DateTimeFormatterBuilder()
      .appendValue(IsoFields.WEEK_BASED_YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
      .appendLiteral("/W")
      .appendValue(IsoFields.WEEK_OF_WEEK_BASED_YEAR, 2)
      .appendLiteral('/')
      .appendValue(DAY_OF_WEEK, 1)
      .toFormatter


    /**
     * Shard TS based on date (e.g. 2010-01-01 => 2010/01/01)
     * Sharding is done on UTC with '/' as separator.
     *
     * @return date shard without leading or trailing separator
     */
    def byDate(ts: ZonedDateTime): String = {
      toUTC(ts).format(DateTimeFormatter.ofPattern("yyyy'/'MM'/'dd"))
    }

    /**
     * Shard TS based on month (e.g. 2010-01-01 => 2010/01)
     * Sharding is done on UTC with '/' as separator.
     *
     * @return month shard without leading or trailing separator
     */
    def byMonth(ts: ZonedDateTime): String = {
      toUTC(ts).format(DateTimeFormatter.ofPattern("yyyy'/'MM"))
    }

    /**
     * Shard TS based on year (e.g. 2010-01-01 => 2010)
     * Sharding is done on UTC with '/' as separator.
     *
     * @return year shard without leading or trailing separator
     */
    def byYear(ts: ZonedDateTime): String = {
      toUTC(ts).format(DateTimeFormatter.ofPattern("yyyy"))
    }

    /**
     * Shard TS based on ISO-8601 week (e.g. 2010-01-01 => 2009/W53)
     * Sharding is done on UTC with '/' as separator.
     *
     * @return ISO-8601 week shard without leading or trailing separator
     */
    def byWeek(ts: ZonedDateTime): String = {
      toUTC(ts).format(shardISOWeekFormat)
    }

    /**
     * Shard TS based on ISO-8601 week date (e.g. 2010-01-01 => 2009/W53/5)
     * Sharding is done on UTC with '/' as separator.
     *
     * @return ISO-8601 week date shard without leading or trailing separator
     */
    def byWeekDate(ts: ZonedDateTime): String = {
      toUTC(ts).format(shardISOWeekDayFormat)
    }
  }
}
