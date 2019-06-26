/*
 * Copyright 2019 E257.FI
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
package fi.e257.tackler.math

import java.math.MathContext

import org.scalatest.FunSpec

class TacklerRealTest extends FunSpec {

  val bd1 = TacklerReal("1000000000000000000000000.1")
  val bd2 = TacklerReal("2000000000000000000000.0002")
  val bd3 = TacklerReal("3000000000000000000.0000003")
  val bd4 = TacklerReal("4000000000000000.0000000004")
  val bd5 = TacklerReal("5000000000000.0000000000005")
  val bd6 = TacklerReal("6000000000.0000000000000006")
  val bd7 = TacklerReal("7000000.0000000000000000007")
  val bd8 = TacklerReal("8000.0000000000000000000008")
  val bd9 = TacklerReal("9.0000000000000000000000009")

  val bdSumStr = "1002003004005006007008009.1002003004005006007008009"

  describe("TacklerReal") {

    it ("ok:  bd9") {
      val v = bd9
      val res = "9.0000000000000000000000009"

      assert(v.scale === 25)
      assert(v === scala.math.BigDecimal(res))
      assert(v.toString() === res)
    }

    it ("ok:  bd8 + bd9") {
      val v = bd8 + bd9
      val res = "8009.0000000000000000000008009"

      assert(v.scale === 25)
      assert(v === scala.math.BigDecimal(res))
      assert(v.toString() === res)
    }

    it ("ok:  bd7 + bd8 + bd9") {
      val v = bd7 + bd8 + bd9
      val res = "7008009.0000000000000000007008009"

      assert(v.scale === 25)
      assert(v === scala.math.BigDecimal(res))
      assert(v.toString() === res)
    }

    it ("ok:  bd6 + bd7 + bd8 + bd9") {
      val v = bd6 + bd7 + bd8 + bd9
      val res = "6007008009.0000000000000006007008009"

      assert(v.scale === 25)
      assert(v === scala.math.BigDecimal(res))
      assert(v.toString() === res)
    }

    it ("ok:  bd5 + bd6 + bd7 + bd8 + bd9") {
      val v = bd5 + bd6 + bd7 + bd8 + bd9
      val res = "5006007008009.0000000000005006007008009"

      assert(v.scale === 25)
      assert(v === scala.math.BigDecimal(res))
      assert(v.toString() === res)
    }

    it ("ok:  bd4 + bd5 + bd6 + bd7 + bd8 + bd9") {
      val v = bd4 + bd5 + bd6 + bd7 + bd8 + bd9
      val res = "4005006007008009.0000000004005006007008009"

      assert(v.scale === 25)
      assert(v === scala.math.BigDecimal(res))
    }

    it ("ok:  bd3 + bd4 + bd5 + bd6 + bd7 + bd8 + bd9") {
      val v = bd3 + bd4 + bd5 + bd6 + bd7 + bd8 + bd9
      val res = "3004005006007008009.0000003004005006007008009"

      assert(v.scale === 25)
      assert(v === scala.math.BigDecimal(res))
      assert(v.toString() === res)
    }

    it ("ok:  bd2 + bd3 + bd4 + bd5 + bd6 + bd7 + bd8 + bd9") {
      val v = bd2 + bd3 + bd4 + bd5 + bd6 + bd7 + bd8 + bd9
      val res = "2003004005006007008009.0002003004005006007008009"

      assert(v.scale === 25)
      assert(v === scala.math.BigDecimal(res))
      assert(v.toString() === res)
    }

    it ("ok:  bd1 + bd2 + bd3 + bd4 + bd5 + bd6 + bd7 + bd8 + bd9") {
      val v = bd1 + bd2 + bd3 + bd4 + bd5 + bd6 + bd7 + bd8 + bd9

      assert(v.scale === 25)
      assert(v === scala.math.BigDecimal(bdSumStr))
      assert(v.toString() === bdSumStr)
    }

    it ("ok:  ZERO == bdSum - bd1 - bd2 - bd3 - bd4 - bd5 - bd6 - bd7 - bd8 - bd9") {
      val v = TacklerReal(bdSumStr) - bd1 - bd2 - bd3 - bd4 - bd5 - bd6 - bd7 - bd8 - bd9

      assert(v.scale === 25)
      assert(v.isZero)
    }
  }

  describe("tackler.math sequence ops") {

    /**
     * Scala bug: BigDecimal and IterableOnce.sum #11592
     * https://github.com/scala/bug/issues/11592
     */
    it("realSum") {
      val bds = List(bd1, bd2, bd3, bd4, bd5, bd6, bd7, bd8, bd9)
      val bdSum = bds.realSum

      assert(bdSum.scale === 25)
      assert(bdSum === scala.math.BigDecimal(bdSumStr))
      assert(bdSum.toString() === bdSumStr)
    }

    it("realSum of empty") {
      val bds = List.empty[TacklerReal]
      val bdSum = bds.realSum

      assert(bdSum === scala.math.BigDecimal(0))
      assert(bdSum.toString() === "0")
    }
  }


  describe("tackler.math BigDecimal ops") {

    describe("isZero") {
      it ("with different 0s") {
        assert(TacklerReal(0).isZero === true)
        assert(TacklerReal(0L).isZero === true)
        assert(TacklerReal(0.0).isZero === true)
        assert(TacklerReal("0").isZero === true)
        assert(ZERO.isZero === true)
      }

      it ("ZERO == BigDecimal(0)") {
        assert(ZERO === BigDecimal(0))
        assert(ZERO === BigDecimal(0, MathContext.DECIMAL32))
        assert(ZERO === BigDecimal(0, MathContext.DECIMAL64))
        assert(ZERO === BigDecimal(0, MathContext.DECIMAL128))
        assert(ZERO === BigDecimal(0, MathContext.UNLIMITED))
      }

      it ("ZERO == BigDecimal(0L)") {
        assert(ZERO === BigDecimal(0L))
        assert(ZERO === BigDecimal(0L, MathContext.DECIMAL32))
        assert(ZERO === BigDecimal(0L, MathContext.DECIMAL64))
        assert(ZERO === BigDecimal(0L, MathContext.DECIMAL128))
        assert(ZERO === BigDecimal(0L, MathContext.UNLIMITED))
      }

      it ("ZERO == BigDecimal(0.0)") {
        assert(ZERO === BigDecimal(0.0))
        assert(ZERO === BigDecimal(0.0, MathContext.DECIMAL32))
        assert(ZERO === BigDecimal(0.0, MathContext.DECIMAL64))
        assert(ZERO === BigDecimal(0.0, MathContext.DECIMAL128))
        assert(ZERO === BigDecimal(0.0, MathContext.UNLIMITED))
      }

      it ("""ZERO == BigDecimal("0")""") {
        assert(ZERO === BigDecimal("0"))
        assert(ZERO === BigDecimal("0", MathContext.DECIMAL32))
        assert(ZERO === BigDecimal("0", MathContext.DECIMAL64))
        assert(ZERO === BigDecimal("0", MathContext.DECIMAL128))
        assert(ZERO === BigDecimal("0", MathContext.UNLIMITED))
      }


      it ("isZero with small increments") {
        val bd1 = TacklerReal("1")
        val bd_99 = TacklerReal("0.9999999999999999999999999")
        val bd__1 = TacklerReal("0.0000000000000000000000001")

        assert((bd1 - bd_99).isZero === false)
        assert((bd1 - bd_99 - bd__1).isZero === true)
      }

      it ("isZero with big and small increments") {
        val big_1 =   TacklerReal("1000000000000000000000000")
        val big_9 =   TacklerReal( "999999999999999999999999")

        val small_9 = TacklerReal("0.9999999999999999999999999")
        val small_1 = TacklerReal("0.0000000000000000000000001")

        assert((big_1 - big_9).isZero === false)
        assert((big_1 - big_9 - small_9).isZero === false)
        assert((big_1 - big_9 - small_9 - small_1).isZero === true)
      }
    }
  }
}
