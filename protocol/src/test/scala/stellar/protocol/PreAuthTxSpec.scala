package stellar.protocol

import okio.ByteString
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class PreAuthTxSpec extends Specification with ScalaCheck {
  import PreAuthTxs._

  "pre authorised transaction" should {
    "encode and decode" >> prop { preAuthTx: PreAuthTx =>
      val (remaining, value) = PreAuthTx.decode.run(preAuthTx.encode).value
      remaining must beEmpty
      value mustEqual preAuthTx
    }

    "encode to string" >> prop { preAuthTx: PreAuthTx =>
      PreAuthTx(preAuthTx.encodeToString) mustEqual preAuthTx
    }
  }
}

object PreAuthTxs {
  implicit val arbAccountId: Arbitrary[PreAuthTx] = Arbitrary(
    Gen.containerOfN[Array, Byte](32, Arbitrary.arbByte.arbitrary)
      .map(bs => PreAuthTx(new ByteString(bs)))
  )
}
