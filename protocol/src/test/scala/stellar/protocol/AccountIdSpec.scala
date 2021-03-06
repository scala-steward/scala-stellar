package stellar.protocol

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class AccountIdSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import AccountIds._

  "account id" should {
    "encode and decode" >> prop { accountId: AccountId =>
      // accountId must xdrDecodeAndEncode(AccountId)
      pending
    }

    "encode to string" >> prop { accountId: AccountId =>
      AccountId(accountId.encodeToString) mustEqual accountId
    }
  }
}

object AccountIds {
  val genAccountId: Gen[AccountId] = Gen.const(() => Seed.random.accountId).map(_.apply())
  implicit val arbAccountId: Arbitrary[AccountId] = Arbitrary(genAccountId)
}
