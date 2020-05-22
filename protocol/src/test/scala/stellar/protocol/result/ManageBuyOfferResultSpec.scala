package stellar.protocol.result

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.XdrSerdeMatchers
import stellar.protocol.ledger.LedgerEntryDatas

class ManageBuyOfferResultSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import ManageBuyOfferResults._

  "manage buy offer result" should {
    "serialise and deserialise" >> prop { result: ManageBuyOfferResult =>
      result must xdrDecodeAndEncode(ManageBuyOfferResult)
    }
  }
}

object ManageBuyOfferResults {
  def genManageBuyOfferCreated: Gen[ManageBuyOfferCreated] = for {
    claims <- Gen.listOf(OfferClaims.genOfferClaim)
    entry <- LedgerEntryDatas.genOfferEntry
  } yield ManageBuyOfferCreated(claims, entry)

  def genManageBuyOfferUpdated: Gen[ManageBuyOfferUpdated] = for {
    claims <- Gen.listOf(OfferClaims.genOfferClaim)
    entry <- LedgerEntryDatas.genOfferEntry
  } yield ManageBuyOfferUpdated(claims, entry)

  def genManageBuyOfferDeleted: Gen[ManageBuyOfferDeleted] = for {
    claims <- Gen.listOf(OfferClaims.genOfferClaim)
  } yield ManageBuyOfferDeleted(claims)

  def genManagedBuyOfferResult: Gen[ManageBuyOfferResult] = Gen.oneOf(
    genManageBuyOfferCreated,
    genManageBuyOfferUpdated,
    genManageBuyOfferDeleted,
    Gen.const(ManageBuyOfferMalformed),
    Gen.const(ManageBuyOfferBuyNoTrust),
    Gen.const(ManageBuyOfferSellNoTrust),
    Gen.const(ManageBuyOfferBuyNoAuth),
    Gen.const(ManageBuyOfferSellNoAuth),
    Gen.const(ManageBuyOfferBuyNoIssuer),
    Gen.const(ManageBuyOfferSellNoIssuer),
    Gen.const(ManageBuyOfferLineFull),
    Gen.const(ManageBuyOfferUnderfunded),
    Gen.const(ManageBuyOfferCrossSelf),
    Gen.const(ManageBuyOfferLowReserve),
    Gen.const(UpdateBuyOfferIdNotFound)
  )
  implicit val arbManagedBuyOfferResult: Arbitrary[ManageBuyOfferResult] =
    Arbitrary(genManagedBuyOfferResult)
}
