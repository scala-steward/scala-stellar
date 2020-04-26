package stellar.protocol.ledger

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.XdrSerdeMatchers

class LiabilitySumSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import LiabilitySums._

  "liabilities" should {
    "serialise and deserialise" >> prop { liabilities: LiabilitySum =>
      liabilities must xdrDecodeAndEncode(LiabilitySum)
    }
  }
}

object LiabilitySums {
  val genLiabilitySum: Gen[LiabilitySum] = for {
    buying <- Gen.posNum[Long]
    selling <- Gen.posNum[Long]
  } yield LiabilitySum(buying, selling)
  implicit val arbLiabilitySum: Arbitrary[LiabilitySum] = Arbitrary(genLiabilitySum)
}