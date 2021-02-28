package stellar.protocol

import okio.ByteString
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class SignatureSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import Signatures._

  "signatures" should {
    "encode and decode" >> prop { signature: Signature =>
      // signature must xdrDecodeAndEncode(Signature)
      pending
    }
  }
}

object Signatures {
  implicit val arbSignature: Arbitrary[Signature] = Arbitrary(
    for {
      data <- Gen.containerOfN[Array, Byte](32, Arbitrary.arbByte.arbitrary)
      hint <- Gen.containerOfN[Array, Byte](4, Arbitrary.arbByte.arbitrary)
    } yield Signature(new ByteString(data), new ByteString(hint))
  )
}
