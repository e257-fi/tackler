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

import java.time.ZonedDateTime

import org.scalatest.funspec.AnyFunSpec

class TxnTSTest extends AnyFunSpec {

  def txt2ts(txtTS: String): ZonedDateTime = {
    ZonedDateTime.parse(txtTS,
      java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
  }

  describe("ISO-8601 timestamps utilities") {
    describe("Format to String") {

      it("isoDate") {
        assert(TxnTS.isoDate(txt2ts("2010-01-02T00:00:00Z")) === "2010-01-02Z")
        assert(TxnTS.isoDate(txt2ts("2010-01-02T00:00:00+02:00")) === "2010-01-02+02:00")
        assert(TxnTS.isoDate(txt2ts("2010-01-02T00:00:00-02:00")) === "2010-01-02-02:00")
      }

      it("isoMonth") {
        assert(TxnTS.isoMonth(txt2ts("2010-01-02T00:00:00Z")) === "2010-01Z")
        assert(TxnTS.isoMonth(txt2ts("2010-01-02T00:00:00+02:00")) === "2010-01+02:00")
        assert(TxnTS.isoMonth(txt2ts("2010-01-02T00:00:00-02:00")) === "2010-01-02:00")
      }

      it("isoYear") {
        assert(TxnTS.isoYear(txt2ts("2010-01-02T00:00:00Z")) === "2010Z")
        assert(TxnTS.isoYear(txt2ts("2010-01-02T00:00:00+02:00")) === "2010+02:00")
        assert(TxnTS.isoYear(txt2ts("2010-01-02T00:00:00-02:00")) === "2010-02:00")
      }

      it("isoZonedTS") {
        assert(TxnTS.isoZonedTS(txt2ts("2010-01-01T00:00:00+16:00")) === "2010-01-01T00:00:00+16:00")
        assert(TxnTS.isoZonedTS(txt2ts("2010-01-01T01:02:03.456+16:00")) === "2010-01-01T01:02:03.456+16:00")
        assert(TxnTS.isoZonedTS(txt2ts("2010-01-01T01:02:03.456789+16:00")) === "2010-01-01T01:02:03.456789+16:00")
        assert(TxnTS.isoZonedTS(txt2ts("2010-01-01T01:02:03.700-16:00")) === "2010-01-01T01:02:03.7-16:00")

        assert(TxnTS.isoZonedTS(txt2ts("2010-01-01T00:00:00+00:00")) === "2010-01-01T00:00:00Z")
      }

      it("isoWeek") {
        assert(TxnTS.isoWeek(txt2ts("2010-01-03T00:00:00+00:00")) === "2009-W53Z")
        assert(TxnTS.isoWeek(txt2ts("2010-01-04T00:00:00+00:00")) === "2010-W01Z")
        assert(TxnTS.isoWeek(txt2ts("2017-01-01T00:00:00+00:00")) === "2016-W52Z")
        assert(TxnTS.isoWeek(txt2ts("2017-01-02T00:00:00+00:00")) === "2017-W01Z")

        assert(TxnTS.isoWeek(txt2ts("2017-01-02T00:00:00+02:00")) === "2017-W01+02:00")
        assert(TxnTS.isoWeek(txt2ts("2017-01-02T00:00:00-02:00")) === "2017-W01-02:00")
      }

      it("isoWeekDate") {
        assert(TxnTS.isoWeekDate(txt2ts("2010-01-03T00:00:00+00:00")) === "2009-W53-7Z")
        assert(TxnTS.isoWeekDate(txt2ts("2010-01-04T00:00:00+00:00")) === "2010-W01-1Z")
        assert(TxnTS.isoWeekDate(txt2ts("2017-01-01T00:00:00+00:00")) === "2016-W52-7Z")
        assert(TxnTS.isoWeekDate(txt2ts("2017-01-02T00:00:00+00:00")) === "2017-W01-1Z")
        assert(TxnTS.isoWeekDate(txt2ts("2017-01-02T00:00:00Z")) === "2017-W01-1Z")

        assert(TxnTS.isoWeekDate(txt2ts("2017-01-02T00:00:00+02:00")) === "2017-W01-1+02:00")
        assert(TxnTS.isoWeekDate(txt2ts("2017-01-02T00:00:00-02:00")) === "2017-W01-1-02:00")
      }
    }
  }


  describe("TxnTS.Shard") {

    describe("byDate must") {
      it("Use UTC for sharding") {
        assert(TxnTS.Shard.byDate(txt2ts("2017-01-01T00:00:00Z")) === "2017/01/01")

        // east to UTC
        assert(TxnTS.Shard.byDate(txt2ts("2017-01-01T00:00:00+02:00")) === "2016/12/31")
        assert(TxnTS.Shard.byDate(txt2ts("2017-01-01T01:59:59+02:00")) === "2016/12/31")
        assert(TxnTS.Shard.byDate(txt2ts("2017-01-01T02:00:00+02:00")) === "2017/01/01")

        // west to UTC
        assert(TxnTS.Shard.byDate(txt2ts("2016-12-31T21:59:59-02:00")) === "2016/12/31")
        assert(TxnTS.Shard.byDate(txt2ts("2016-12-31T22:00:00-02:00")) === "2017/01/01")
        assert(TxnTS.Shard.byDate(txt2ts("2017-01-01T00:00:00-02:00")) === "2017/01/01")
      }
    }


    describe("byMonth must") {
      it("Use UTC for sharding") {
        assert(TxnTS.Shard.byMonth(txt2ts("2017-01-01T00:00:00Z")) === "2017/01")

        // east to UTC
        assert(TxnTS.Shard.byMonth(txt2ts("2017-01-01T00:00:00+02:00")) === "2016/12")
        assert(TxnTS.Shard.byMonth(txt2ts("2017-01-01T01:59:59+02:00")) === "2016/12")
        assert(TxnTS.Shard.byMonth(txt2ts("2017-01-01T02:00:00+02:00")) === "2017/01")

        // west to UTC
        assert(TxnTS.Shard.byMonth(txt2ts("2016-12-31T21:59:59-02:00")) === "2016/12")
        assert(TxnTS.Shard.byMonth(txt2ts("2016-12-31T22:00:00-02:00")) === "2017/01")
        assert(TxnTS.Shard.byMonth(txt2ts("2017-01-01T00:00:00-02:00")) === "2017/01")
      }
    }

    describe("byYear must") {
      it("Use UTC for sharding") {
        assert(TxnTS.Shard.byYear(txt2ts("2017-01-01T00:00:00Z")) === "2017")

        // east to UTC
        assert(TxnTS.Shard.byYear(txt2ts("2017-01-01T00:00:00+02:00")) === "2016")
        assert(TxnTS.Shard.byYear(txt2ts("2017-01-01T01:59:59+02:00")) === "2016")
        assert(TxnTS.Shard.byYear(txt2ts("2017-01-01T02:00:00+02:00")) === "2017")

        // west to UTC
        assert(TxnTS.Shard.byYear(txt2ts("2016-12-31T21:59:59-02:00")) === "2016")
        assert(TxnTS.Shard.byYear(txt2ts("2016-12-31T22:00:00-02:00")) === "2017")
        assert(TxnTS.Shard.byYear(txt2ts("2017-01-01T00:00:00-02:00")) === "2017")
      }
    }


    describe("byWeek must") {
      it("Handle tricky years (2010, 2017, ...)") {
        assert(TxnTS.Shard.byWeek(txt2ts("2010-01-03T00:00:00+00:00")) === "2009/W53")
        assert(TxnTS.Shard.byWeek(txt2ts("2017-01-01T00:00:00+00:00")) === "2016/W52")
      }

      it("Start week on Monday") {
        assert(TxnTS.Shard.byWeek(txt2ts("2010-01-04T00:00:00+00:00")) === "2010/W01")
        assert(TxnTS.Shard.byWeek(txt2ts("2017-01-02T00:00:00+00:00")) === "2017/W01")
      }

      it("Use UTC for sharding") {
        assert(TxnTS.Shard.byWeek(txt2ts("2017-01-02T00:00:00Z")) === "2017/W01")

        // east to UTC
        assert(TxnTS.Shard.byWeek(txt2ts("2017-01-02T00:00:00+02:00")) === "2016/W52")
        assert(TxnTS.Shard.byWeek(txt2ts("2017-01-02T01:59:59+02:00")) === "2016/W52")
        assert(TxnTS.Shard.byWeek(txt2ts("2017-01-02T02:00:00+02:00")) === "2017/W01")

        // west to UTC
        assert(TxnTS.Shard.byWeek(txt2ts("2017-01-01T21:59:59-02:00")) === "2016/W52")
        assert(TxnTS.Shard.byWeek(txt2ts("2017-01-01T22:00:00-02:00")) === "2017/W01")
        assert(TxnTS.Shard.byWeek(txt2ts("2017-01-02T00:00:00-02:00")) === "2017/W01")
      }
    }

    describe("byWeekDate must") {
      it("Handle tricky years  (2010, 2017, ...)") {
        assert(TxnTS.Shard.byWeekDate(txt2ts("2010-01-03T00:00:00+00:00")) === "2009/W53/7")
        assert(TxnTS.Shard.byWeekDate(txt2ts("2017-01-01T00:00:00+00:00")) === "2016/W52/7")
      }

      it("Start week on Monday") {
        assert(TxnTS.Shard.byWeekDate(txt2ts("2010-01-04T00:00:00+00:00")) === "2010/W01/1")
        assert(TxnTS.Shard.byWeekDate(txt2ts("2017-01-02T00:00:00+00:00")) === "2017/W01/1")
      }

      it("Use UTC for sharding") {
        assert(TxnTS.Shard.byWeekDate(txt2ts("2017-01-02T00:00:00Z")) === "2017/W01/1")

        // east to UTC
        assert(TxnTS.Shard.byWeekDate(txt2ts("2017-01-02T00:00:00+02:00")) === "2016/W52/7")
        assert(TxnTS.Shard.byWeekDate(txt2ts("2017-01-02T01:59:59+02:00")) === "2016/W52/7")
        assert(TxnTS.Shard.byWeekDate(txt2ts("2017-01-02T02:00:00+02:00")) === "2017/W01/1")

        // west to UTC
        assert(TxnTS.Shard.byWeekDate(txt2ts("2017-01-01T21:59:59-02:00")) === "2016/W52/7")
        assert(TxnTS.Shard.byWeekDate(txt2ts("2017-01-01T22:00:00-02:00")) === "2017/W01/1")
        assert(TxnTS.Shard.byWeekDate(txt2ts("2017-01-02T00:00:00-02:00")) === "2017/W01/1")
      }
    }

  }
}
