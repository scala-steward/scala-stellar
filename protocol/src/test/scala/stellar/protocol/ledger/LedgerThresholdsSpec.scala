package stellar.protocol.ledger

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.XdrSerdeMatchers

class LedgerThresholdsSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import LedgerThresholds._

  "ledger threasholds" should {
    "encode and decode" >> prop { ledgerThreshold: LedgerThreshold =>
      ledgerThreshold must xdrDecodeAndEncode(LedgerThreshold)
    }
  }
}

object LedgerThresholds {
  val genLedgerThreshold: Gen[LedgerThreshold] = for {
    master <- Gen.choose(0, 255)
    low <- Gen.choose(0, 255)
    med <- Gen.choose(0, 255)
    high <- Gen.choose(0, 255)
  } yield LedgerThreshold(master, low, med, high)
  implicit val arbLedgerThreshold: Arbitrary[LedgerThreshold] = Arbitrary(genLedgerThreshold)
}
