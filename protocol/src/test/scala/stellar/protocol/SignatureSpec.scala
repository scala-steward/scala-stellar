package stellar.protocol

import okio.ByteString
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class SignatureSpec extends Specification with ScalaCheck {
  import Signatures._

  "signatures" should {
    "encode and decode" >> prop { signature: Signature =>
      val (remaining, value) = Signature.decode.run(signature.encode).value
      remaining must beEmpty
      value mustEqual signature
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
