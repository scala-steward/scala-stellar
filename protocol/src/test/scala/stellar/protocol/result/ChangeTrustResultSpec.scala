package stellar.protocol.result

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.XdrSerdeMatchers

class ChangeTrustResultSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import ChangeTrustResults._

  "change trust result" should {
    "serialise and deserialise" >> prop { result: ChangeTrustResult =>
      result must xdrDecodeAndEncode(ChangeTrustResult)
    }
  }
}

object ChangeTrustResults {
  def genChangeTrustResult: Gen[ChangeTrustResult] = Gen.oneOf(
    ChangeTrustSuccess, ChangeTrustInvalidLimit, ChangeTrustLowReserve, ChangeTrustMalformed,
    ChangeTrustNoIssuer, ChangeTrustSelfNotAllowed
  )
  implicit val arbChangeTrustResult: Arbitrary[ChangeTrustResult] = Arbitrary(genChangeTrustResult)
}
