package stellar.protocol.result

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.XdrSerdeMatchers

class CreatePassiveSellOfferResultSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import CreatePassiveSellOfferResults._

  "create account result" should {
    "serialise and deserialise" >> prop { result: CreatePassiveSellOfferResult =>
      result must xdrDecodeAndEncode(CreatePassiveSellOfferResult)
    }
  }
}

object CreatePassiveSellOfferResults {
  def genCreatePassiveSellOfferResult: Gen[CreatePassiveSellOfferResult] = Gen.oneOf(
    CreatePassiveSellOfferSuccess,
    CreatePassiveSellOfferMalformed,
    CreatePassiveSellOfferSellNoTrust,
    CreatePassiveSellOfferBuyNoTrust,
    CreatePassiveSellOfferSellNoAuth,
    CreatePassiveSellOfferBuyNoAuth,
    CreatePassiveSellOfferLineFull,
    CreatePassiveSellOfferUnderfunded,
    CreatePassiveSellOfferCrossSelf,
    CreatePassiveSellOfferSellNoIssuer,
    CreatePassiveSellOfferBuyNoIssuer,
    UpdatePassiveOfferIdNotFound,
    CreatePassiveSellOfferLowReserve
  )
  implicit val arbCreatePassiveSellOfferResult: Arbitrary[CreatePassiveSellOfferResult] =
    Arbitrary(genCreatePassiveSellOfferResult)
}