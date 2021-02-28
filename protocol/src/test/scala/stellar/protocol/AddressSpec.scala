package stellar.protocol

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class AddressSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import Addresses._

  "an address" should {
    "serialise and deserialise" >> prop { address: Address =>
      // address must xdrDecodeAndEncode(Address)
      pending
    }

    "encode and decode to the same" >> prop { address: Address =>
      Address(address.accountId.encodeToString) mustEqual address
    }
  }
}

object Addresses {
  import AccountIds._

  def genAddress: Gen[Address] = for {
    accountId <- genAccountId
    // subAccountId <- Gen.option(Gen.posNum[Long]) // TODO - SEP23
  } yield Address(accountId)

  implicit val arbAddress: Arbitrary[Address] = Arbitrary(genAddress)
}