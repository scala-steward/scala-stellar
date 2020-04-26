package stellar.protocol.ledger

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.{AccountIds, Assets, XdrSerdeMatchers}

class LedgerKeySpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import LedgerKeys._

  "ledger key" should {
    "encode and decode" >> prop { ledgerKey: LedgerKey =>
      ledgerKey must xdrDecodeAndEncode(LedgerKey)
    }
  }
}

object LedgerKeys {
  val genLedgerKey: Gen[LedgerKey] = for {
    accountId <- AccountIds.genAccountId
    key <- Gen.chooseNum(0, 3).flatMap {
      case 0 => Gen.const(AccountKey(accountId))
      case 1 => Assets.genToken.map(TrustLineKey(accountId, _))
      case 2 => Gen.posNum[Long].map(OfferKey(accountId, _))
      case 3 => Gen.identifier.map(DataKey(accountId, _))
    }
  } yield key
  implicit val arbLedgerKey: Arbitrary[LedgerKey] = Arbitrary(genLedgerKey)
}
