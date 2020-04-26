package stellar.protocol.result

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.XdrSerdeMatchers

class PaymentResultSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import PaymentResults._

  "payment result" should {
    "serialise and deserialise" >> prop { paymentResult: PaymentResult =>
      paymentResult must xdrDecodeAndEncode(PaymentResult)
    }
  }
}

object PaymentResults {
  val genPaymentResult: Gen[PaymentResult] = Gen.oneOf(
    PaymentSuccess,
    PaymentMalformed,
    PaymentUnderfunded,
    PaymentSourceNoTrust,
    PaymentSourceNotAuthorised,
    PaymentNoDestination,
    PaymentDestinationNoTrust,
    PaymentDestinationNotAuthorised,
    PaymentDestinationLineFull,
    PaymentNoIssuer,
  )
  implicit val arbPaymentResult: Arbitrary[PaymentResult] = Arbitrary(genPaymentResult)
}