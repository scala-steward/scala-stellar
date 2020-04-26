package stellar.protocol

import net.i2p.crypto.eddsa.{EdDSAPublicKey, KeyPairGenerator}
import okio.ByteString
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class AccountIdSpec extends Specification with ScalaCheck {
  import AccountIds._

  "account id" should {
    "encode and decode" >> prop { accountId: AccountId =>
      val (remaining, value) = AccountId.decode.run(accountId.encode).value
      remaining must beEmpty
      value mustEqual accountId
    }

    "encode to string" >> prop { accountId: AccountId =>
      AccountId(accountId.encodeToString) mustEqual accountId
    }
  }
}

object AccountIds {
  val genAccountId: Gen[AccountId] = Gen.oneOf(Seq(() =>
    AccountId(new ByteString(new KeyPairGenerator().generateKeyPair().getPublic.asInstanceOf[EdDSAPublicKey].getAbyte))))
    .map(_.apply())
  implicit val arbAccountId: Arbitrary[AccountId] = Arbitrary(genAccountId)
}
