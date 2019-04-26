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

import org.scalatest.FlatSpec
import fi.e257.tackler.core.{CommodityException, Settings}

class TacklerParserCommoditiesTest extends FlatSpec {

  val tt = new TacklerTxns(Settings())

  behavior of "Units and Commodities"

  /**
   * parse-only test
   * test: aadbdf7c-c1d0-4e1e-a02f-9ca1b5ab2afc
   */
  it should "commodity names" in {
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
  it should "uac ; comment" in {
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
  it should "closing position" in {
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
  it should "uac closing position ; comment" in {
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

  behavior of "Profit and Loss parsing"

  /**
   * test:uuid: 9f711991-c9ae-4558-923c-95a69faff8bc
   */
  it should "opening with PnL" in {
    val txnStr =
      """
        |2017-01-01
        | e   1 USD {1.20 EUR}
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
    assert(txns.txns.size === 8)
  }

  /**
   * test:uuid: 92f75975-061b-4867-87f5-e25cf5b13d40
   */
  it should "opening with PnL ; comment" in {
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
   * test:uuid: 84d81380-8664-45d7-a9e1-523c38c7a963
   */
  it should "closing position with PnL" in {
    val txnStr =
      """
        |2017-01-01
        | e   1 USD {1.20 EUR} @ 1.09 EUR
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
    assert(txns.txns.size === 8)
  }

  /**
   * test:uuid: c1fbac7b-e924-4eee-aed3-b11b51116f1a
   */
  it should "closing position with PnL ; comment" in {
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

  behavior of "with invalid input"
  /**
    * test:uuid: 20b89e3e-a987-4e83-bd89-2cbf288caecc
    */
  it should "discrepancy of commodities '='" in {
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

  /**
    * test:uuid: fe246259-2280-4d42-8360-6dd3e280b30a
    */
  it should "discrepancy of commodities '@'" in {
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
    * test:uuid: 6d1868da-3b9f-45e4-a2c6-db003da4c720
    */
  it should "value pos: same primary and secondary commodity ('@')" in {
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
    * test:uuid: aa52ac0a-278a-49e4-abad-fc2f00416a41
    */
  it should "value pos: same primary and secondary commodity  ('=')" in {
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
   * test:uuid: 4babf379-9d88-49f3-8158-b9b7ff4e6eed
   */
  it should "perr: duplicate commodity" in {
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
    * test:uuid: e24aacdf-fba2-4dc7-8165-4270c8822559
    */
  it should "perr: value position, no primary commodity" in {
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
   * test:uuid: 0d1beaf2-c30c-4008-943f-46aaf44e4f76
   */
  it should "perr: value position, no secondary commodity" in {
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
   * test:uuid: 3152ec2f-4d5f-4a0a-b88c-68f17bccf7c6
   */
  it should "perr: missing value pos value" in {
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
   * test:uuid: bed02ea9-4191-4c98-b847-6b4e2a0fcb2d
   */
  it should "perr: with opening (comm)" in {
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
   * test:uuid: ac4a6183-fb21-4847-8b3e-912f21fe5a6b
   */
  it should "perr: with opening (value)" in {
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
   * test:uuid: 436d9ed5-b7a0-4e37-a7b4-86b00eb60e83
   */
  it should "perr: with missing @" in {
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
