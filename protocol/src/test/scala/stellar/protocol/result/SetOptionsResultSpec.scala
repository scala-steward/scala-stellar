package stellar.protocol.result

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.XdrSerdeMatchers

class SetOptionsResultSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import SetOptionsResults._

  "set options results" should {
    "serialise and deserialise" >> prop { result: SetOptionsResult =>
      result must xdrDecodeAndEncode(SetOptionsResult)
    }
  }
}

object SetOptionsResults {
  def genSetOptionsResult: Gen[SetOptionsResult] = Gen.oneOf(
    SetOptionsSuccess,
    SetOptionsLowReserve,
    SetOptionsTooManySigners,
    SetOptionsBadFlags,
    SetOptionsInvalidInflation,
    SetOptionsCannotChange,
    SetOptionsUnknownFlag,
    SetOptionsThresholdOutOfRange,
    SetOptionsBadSigner,
    SetOptionsInvalidHomeDomain)
  implicit val arbSetOptionsResult: Arbitrary[SetOptionsResult] = Arbitrary(genSetOptionsResult)
}
