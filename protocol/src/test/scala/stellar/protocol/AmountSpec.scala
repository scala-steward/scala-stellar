package stellar.protocol

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class AmountSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import Amounts._

  "amount" should {
    "encode and decode" >> prop { amount: Amount =>
      // amount should xdrDecodeAndEncode(Amount)
      pending
    }
  }
}

object Amounts {
  val genAmount: Gen[Amount] = for {
    asset <- Assets.genAsset
    units <- Gen.posNum[Long]
  } yield Amount(asset, units)
  implicit val arbAmount: Arbitrary[Amount] = Arbitrary(genAmount)
}