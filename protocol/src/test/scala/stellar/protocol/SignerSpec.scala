package stellar.protocol

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class SignerSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import Signers._

  "signer" should {
    "serialise and deserialise" >> prop { signer: Signer =>
      signer must xdrDecodeAndEncode(Signer)
    }
  }
}

object Signers {
  val genSigner: Gen[Signer] = for {
    key <- Gen.oneOf(AccountIds.genAccountId, PreAuthTxs.genPreAuthTx, HashXs.genHashX)
    weight <- Gen.chooseNum(0, 255)
  } yield Signer(key, weight)
  implicit val arbSigner: Arbitrary[Signer] = Arbitrary(genSigner)
}
