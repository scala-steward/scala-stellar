package stellar.protocol.result

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.XdrSerdeMatchers

class CreateAccountResultSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import CreateAccountResults._

  "create account result" should {
    "serialise and deserialise" >> prop { createAccountResult: CreateAccountResult =>
      createAccountResult must xdrDecodeAndEncode(CreateAccountResult)
    }
  }
}

object CreateAccountResults {
  val genCreateAccountResult: Gen[CreateAccountResult] = Gen.oneOf(
    CreateAccountSuccess,
    CreateAccountMalformed,
    CreateAccountUnderfunded,
    CreateAccountLowReserve,
    CreateAccountAlreadyExists
  )
  implicit val arbCreateAccountResult: Arbitrary[CreateAccountResult] = Arbitrary(genCreateAccountResult)
}