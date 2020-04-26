package stellar.protocol.result

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.{AccountIds, Amounts, Assets, XdrSerdeMatchers}

class PathPaymentResultSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import PathPaymentResults._

  "payment result" should {
    "serialise and deserialise" >> prop { pathPaymentResult: PathPaymentResult =>
      pathPaymentResult must xdrDecodeAndEncode(PathPaymentResult)
    }
  }
}

object PathPaymentResults {
  val genPathPaymentSuccess: Gen[PathPaymentSuccess] = for {
    claims <- Gen.listOf(OfferClaims.genOfferClaim)
    destination <- AccountIds.genAccountId
    paid <- Amounts.genAmount
  } yield PathPaymentSuccess(claims, destination, paid)
  val genPathPaymentResult: Gen[PathPaymentResult] = Gen.oneOf(
    genPathPaymentSuccess,
    Gen.const(PathPaymentMalformed),
    Gen.const(PathPaymentUnderfunded),
    Gen.const(PathPaymentSourceNoTrust),
    Gen.const(PathPaymentSourceNotAuthorised),
    Gen.const(PathPaymentNoDestination),
    Gen.const(PathPaymentDestinationNoTrust),
    Gen.const(PathPaymentDestinationNotAuthorised),
    Gen.const(PathPaymentDestinationLineFull),
    Assets.genAsset.map(PathPaymentNoIssuer),
    Gen.const(PathPaymentTooFewOffers),
    Gen.const(PathPaymentOfferCrossesSelf),
    Gen.const(PathPaymentSendMaxExceeded),
  )
  implicit val arbPathPaymentResult: Arbitrary[PathPaymentResult] = Arbitrary(genPathPaymentResult)
}