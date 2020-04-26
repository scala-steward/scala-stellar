package stellar.protocol

import okio.ByteString
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class PreAuthTxSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import PreAuthTxs._

  "pre authorised transaction" should {
    "encode and decode" >> prop { preAuthTx: PreAuthTx =>
      preAuthTx must xdrDecodeAndEncode(PreAuthTx)
    }

    "encode to string" >> prop { preAuthTx: PreAuthTx =>
      PreAuthTx(preAuthTx.encodeToString) mustEqual preAuthTx
    }
  }
}

object PreAuthTxs {
  implicit val arbPreAuthTx: Arbitrary[PreAuthTx] = Arbitrary(
    Gen.containerOfN[Array, Byte](32, Arbitrary.arbByte.arbitrary)
      .map(bs => PreAuthTx(new ByteString(bs)))
  )
}
