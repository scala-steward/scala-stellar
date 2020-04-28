package stellar.protocol.result

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.XdrSerdeMatchers

class AllowTrustResultSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import AllowTrustResults._

  "allow trust result" should {
    "serialise and deserialise" >> prop { result: AllowTrustResult =>
      result must xdrDecodeAndEncode(AllowTrustResult)
    }
  }
}

object AllowTrustResults {
  val genAllowTrustResult: Gen[AllowTrustResult] = Gen.oneOf(
    AllowTrustSuccess, AllowTrustCannotRevoke, AllowTrustMalformed, AllowTrustNotRequired,
    AllowTrustNoTrustLine, AllowTrustSelfNotAllowed
  )
  implicit val arbAllowTrustResult: Arbitrary[AllowTrustResult] = Arbitrary(genAllowTrustResult)
}
