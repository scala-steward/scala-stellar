package stellar.protocol

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class AddressSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import Addresses._

  "an address" should {
    "serialise and deserialise" >> prop { address: Address =>
      address must xdrDecodeAndEncode(Address)
    }
  }
}

object Addresses {
  import AccountIds._

  def genAddress: Gen[Address] = for {
    accountId <- genAccountId
    subAccountId <- Gen.option(Gen.posNum[Long])
  } yield Address(accountId, subAccountId)

  implicit val arbAddress: Arbitrary[Address] = Arbitrary(genAddress)
}