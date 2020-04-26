package stellar.protocol.result

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.{AccountIds, Amounts, XdrSerdeMatchers}

class OfferClaimSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import OfferClaims._

  "offer claim" should {
    "serialise and deserialise" >> prop { offerClaim: OfferClaim =>
      offerClaim must xdrDecodeAndEncode(OfferClaim)
    }
  }
}

object OfferClaims {
  val genOfferClaim: Gen[OfferClaim] = for {
    seller <- AccountIds.genAccountId
    offerId <- Gen.posNum[Long]
    sold <- Amounts.genAmount
    bought <- Amounts.genAmount
  } yield OfferClaim(seller, offerId, sold, bought)
  implicit val arbOfferClaim: Arbitrary[OfferClaim] = Arbitrary(genOfferClaim)
}
