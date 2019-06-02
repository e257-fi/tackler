/*
 * Copyright 2017-2019 E257.FI
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

import fi.e257.tackler.core.{CommodityException, Settings}
import org.scalatest.FunSpec

class TacklerParserCommoditiesTest extends FunSpec {

  val tt = new TacklerTxns(Settings())

  describe("Units and Commodities") {

    /**
     * parse-only test
     * test: aadbdf7c-c1d0-4e1e-a02f-9ca1b5ab2afc
     */
    it("commodity names") {
      val txnStr =
        """
          |2017-01-01
          | e   1 USD
          | a
          |
          |2019-01-01
          | e   1 €
          | a
          |
          |2019-01-01
          | e   1 ¢
          | a
          |
          |2019-01-01
          | e   1 $
          | a
          |
          |2019-01-01
          | e   1 £
          | a
          |
          |2019-01-01
          | e   1 ¥
          | a
          |
          |2019-01-01
          | e   1 ¤
          | a
          |
          |2019-01-01
          | e   1 Au·µg
          | a
          |
          |""".stripMargin

      val txns = tt.string2Txns(txnStr)
      assert(txns.txns.size === 8)
    }

    /**
     * parse-only test
     */
    it("uac ; comment") {
      val txnStr =
        """
          |2017-01-01
          | e   1 USD; comment
          | a
          |
          |2017-01-01
          | e   1 USD ; comment
          | a
          |
          |""".stripMargin

      val txns = tt.string2Txns(txnStr)
      assert(txns.txns.size === 2)
    }

    /**
     * parse-only test
     * test: 5f5dcb57-792d-49df-a491-2923612a0e2f
     */
    it("closing position") {
      val txnStr =
        """
          |2017-01-01
          | e   1 USD @ 1.20 EUR
          | a
          |
          |2019-01-01
          | e   1 USD @ 1 €
          | a
          |
          |2019-01-01
          | e   1 € @ 1 $
          | a
          |
          |2019-01-01
          | e   1 $ @ 1 £
          | a
          |
          |2019-01-01
          | e   1 £ @ 1 ¥
          | a
          |
          |2019-01-01
          | e   1 ¥ @ 1 ¢
          | a
          |
          |2019-01-01
          | e   1 ¢ @ 1 Au·µg
          | a
          |
          |2019-01-01
          | e   1 Au·µg @ 1 EUR
          | a
          |
          |""".stripMargin

      val txns = tt.string2Txns(txnStr)
      assert(txns.txns.size === 8)
    }

    /**
     * parse-only test
     */
    it("uac closing position ; comment") {
      val txnStr =
        """
          |2017-01-01
          | e   1 USD @ 1.20 EUR; comment
          | a
          |
          |2017-01-01
          | e   1 USD @ 1.20 EUR ; comment
          | a
          |
          |""".stripMargin

      val txns = tt.string2Txns(txnStr)
      assert(txns.txns.size === 2)
    }
  }

  describe("Profit and Loss parsing") {

    /**
     * test: 9f711991-c9ae-4558-923c-95a69faff8bc
     */
    it("opening with PnL") {
      val txnStr =
        """
          |2017-01-01
          | e   1 USD {1.20 EUR}
          | a
          |
          |2017-01-01
          | e   -1 USD {1.20 EUR}
          | a
          |
          |2019-01-01
          | e   1 USD {1 €}
          | a
          |
          |2019-01-01
          | e   1 € { 1 $ }
          | a
          |
          |2019-01-01
          | e   1 $ {1 £ }
          | a
          |
          |2019-01-01
          | e   1 £ { 1 ¥}
          | a
          |
          |2019-01-01
          | e   1 ¥ {1 ¢}
          | a
          |
          |2019-01-01
          | e   1 ¢ {1 Au·µg}
          | a
          |
          |2019-01-01
          | e   1 Au·µg {1 EUR}
          | a
          |
          |""".stripMargin

      val txns = tt.string2Txns(txnStr)
      assert(txns.txns.size === 9)
    }

    /**
     * test: 92f75975-061b-4867-87f5-e25cf5b13d40
     */
    it("opening with PnL ; comment") {
      val txnStr =
        """
          |2017-01-01
          | e   1 USD {1.20 EUR}; comment
          | a
          |
          |2017-01-01
          | e   1 USD {1.20 EUR} ; comment
          | a
          |
          |""".stripMargin

      val txns = tt.string2Txns(txnStr)
      assert(txns.txns.size === 2)
    }

    /**
     * test: 84d81380-8664-45d7-a9e1-523c38c7a963
     */
    it("closing position with PnL") {
      val txnStr =
        """
          |2017-01-01
          | e   1 USD {1.20 EUR} @ 1.09 EUR
          | a
          |
          |2017-01-01
          | e   -1 USD {1.20 EUR} @ 1.09 EUR
          | a
          |
          |2019-01-01
          | e   1 USD {1 €} @ 1.09 €
          | a
          |
          |2019-01-01
          | e   1 € { 1 $ } @ 1.09 $
          | a
          |
          |2019-01-01
          | e   1 $ {1 £ } @ 1.09 £
          | a
          |
          |2019-01-01
          | e   1 £ { 1 ¥} @ 1.09  ¥
          | a
          |
          |2019-01-01
          | e   1 ¥ {1 ¢} @ 1.09 ¢
          | a
          |
          |2019-01-01
          | e   1 ¢ {1 Au·µg} @ 1.09 Au·µg
          | a
          |
          |2019-01-01
          | e   1 ⁴ {1 EUR} @ 1.09 EUR
          | a
          |
          |""".stripMargin

      val txns = tt.string2Txns(txnStr)
      assert(txns.txns.size === 9)
    }

    /**
     * test: c1fbac7b-e924-4eee-aed3-b11b51116f1a
     */
    it("closing position with PnL ; comment") {
      val txnStr =
        """
          |2017-01-01
          | e   1 USD {1.20 EUR} @ 1.09 EUR; comment
          | a
          |
          |2017-01-01
          | e   1 USD {1.20 EUR} @ 1.09 EUR ; comment
          | a
          |
          |""".stripMargin

      val txns = tt.string2Txns(txnStr)
      assert(txns.txns.size === 2)
    }
  }

  describe("Invalid inputs and errors") {

    describe("Logical errors") {
      /**
       * test: 5af5d0d8-ca6e-4a03-a939-99d9d2a4ec43
       */
      it("Unit cost '{ ... }' with negative value") {
        val txnStr =
          """
            |2017-01-01
            | e   1.12 USD {-1.00 EUR}
            | a
            |
            |""".stripMargin

        val ex = intercept[CommodityException] {
          tt.string2Txns(txnStr)
        }

        assert(ex.getMessage.contains("Unit cost"))
        assert(ex.getMessage.contains("is negative"))
      }

      /**
       * test: a27b166c-e9c9-432c-bb9d-91915b51d76b
       */
      it("Unit price '@' with negative value") {
        val txnStr =
          """
            |2019-01-01
            | e 1 € @ -1.2 $
            | a 1.2 $
            |
            |""".stripMargin

        val ex = intercept[CommodityException] {
          tt.string2Txns(txnStr)
        }
        assert(ex.getMessage.contains("Unit price"))
        assert(ex.getMessage.contains("is negative"))
      }

      /**
       * test: 6d1868da-3b9f-45e4-a2c6-db003da4c720
       */
      it("Unit price '@' with same primary and secondary commodity") {
        val txnStr =
          """
            |2019-01-01
            | e 1 € @ 1 €
            | a
            |
            |""".stripMargin

        val ex = intercept[CommodityException] {
          tt.string2Txns(txnStr)
        }
        assert(ex.getMessage.startsWith("Error on line: 3; Both commodities are same for value position [€]"))
      }

      /**
       * test: fe246259-2280-4d42-8360-6dd3e280b30a
       */
      it("Unit price '@' with discrepancy of commodities") {
        val txnStr =
          """
            |2019-01-01
            | e 1 € @ 1 $
            | a 1 € @ 1 £
            |
            |""".stripMargin

        val ex = intercept[CommodityException] {
          tt.string2Txns(txnStr)
        }
        assert(ex.getMessage.startsWith("Different commodities without"))
      }

      /**
       * test: 6f45f594-c4e6-449a-b6d2-7f25e9479bd5
       */
      it("Total cost '=' with different sign (-1st vs. +2nd)") {
        val txnStr =
          """
            |2019-01-01
            | e -1 $ = 1 €
            | a
            |
            |""".stripMargin

        val ex = intercept[CommodityException] {
          tt.string2Txns(txnStr)
        }
        assert(ex.getMessage.contains("Total cost"))
        assert(ex.getMessage.contains("different sign"))
      }

      /**
       * test: aaf50217-1d04-49bd-a873-43a53be1c99f
       */
      it("Total cost '=' with different sign (+1st vs. -2nd)") {
        val txnStr =
          """
            |2019-01-01
            | e 1 $ = -1 €
            | a
            |
            |""".stripMargin

        val ex = intercept[CommodityException] {
          tt.string2Txns(txnStr)
        }
        assert(ex.getMessage.contains("Total cost"))
        assert(ex.getMessage.contains("different sign"))
      }


      /**
       * test: aa52ac0a-278a-49e4-abad-fc2f00416a41
       */
      it("Total cost '=' with same primary and secondary commodity") {
        val txnStr =
          """
            |2019-01-01
            | e 1 € = 1 €
            | a
            |
            |""".stripMargin

        val ex = intercept[CommodityException] {
          tt.string2Txns(txnStr)
        }
        assert(ex.getMessage.startsWith("Error on line: 3; Both commodities are same for value position [€]"))
      }

      /**
       * test: 20b89e3e-a987-4e83-bd89-2cbf288caecc
       */
      it("Total cost '=' with discrepancy of commodities") {
        val txnStr =
          """
            |2019-01-01
            | e 1 € = 1 $
            | a 1 € = 1 £
            |
            |""".stripMargin

        val ex = intercept[CommodityException] {
          tt.string2Txns(txnStr)
        }
        assert(ex.getMessage.startsWith("Different commodities without"))
      }
    }

    /**
     * test: 4babf379-9d88-49f3-8158-b9b7ff4e6eed
     */
    it("perr: duplicate commodity") {
      val txnStr =
        """
          |2017-01-01
          | e   1 USD EUR
          | a
          |
          |""".stripMargin

      val ex = intercept[TacklerParseException] {
        tt.string2Txns(txnStr)
      }
      assert(ex.getMessage.contains("on line: 3"))
    }

    /**
     * test: e24aacdf-fba2-4dc7-8165-4270c8822559
     */
    it("perr: value position, no primary commodity") {
      val txnStr =
        """
          |2017-01-01
          | e   1 @ 1 EUR
          | a
          |
          |""".stripMargin

      val ex = intercept[TacklerParseException] {
        tt.string2Txns(txnStr)
      }
      assert(ex.getMessage.contains("on line: 3"))
    }

    /**
     * test: 0d1beaf2-c30c-4008-943f-46aaf44e4f76
     */
    it("perr: value position, no secondary commodity") {
      val txnStr =
        """
          |2017-01-01
          | e   1 USD @ 2
          | a
          |
          |""".stripMargin

      val ex = intercept[TacklerParseException] {
        tt.string2Txns(txnStr)
      }
      assert(ex.getMessage.contains("on line: 3"))
    }

    /**
     * test: 3152ec2f-4d5f-4a0a-b88c-68f17bccf7c6
     */
    it("perr: missing value pos value") {
      val txnStr =
        """
          |2017-01-01
          | e   1 USD @ EUR
          | a
          |
          |""".stripMargin

      val ex = intercept[TacklerParseException] {
        tt.string2Txns(txnStr)
      }
      assert(ex.getMessage.contains("on line: 3"))
    }

    /**
     * test: bed02ea9-4191-4c98-b847-6b4e2a0fcb2d
     */
    it("perr: with opening (comm)") {
      val txnStr =
        """
          |2017-01-01
          | e   1 USD {1.00} @ 1.20 EUR
          | a
          |
          |""".stripMargin

      val ex = intercept[TacklerParseException] {
        tt.string2Txns(txnStr)
      }
      assert(ex.getMessage.contains("on line: 3"))
    }

    /**
     * test: ac4a6183-fb21-4847-8b3e-912f21fe5a6b
     */
    it("perr: with opening (value)") {
      val txnStr =
        """
          |2017-01-01
          | e   1 USD {EUR} @ 1.20 EUR
          | a
          |
          |""".stripMargin

      val ex = intercept[TacklerParseException] {
        tt.string2Txns(txnStr)
      }
      assert(ex.getMessage.contains("on line: 3"))
    }

    /**
     * test: 436d9ed5-b7a0-4e37-a7b4-86b00eb60e83
     */
    it("perr: with missing @") {
      val txnStr =
        """
          |2017-01-01
          | e   1 USD {1.00 EUR}  1.20 EUR
          | a
          |
          |""".stripMargin

      val ex = intercept[TacklerParseException] {
        tt.string2Txns(txnStr)
      }
      assert(ex.getMessage.contains("on line: 3"))
    }
  }
}
