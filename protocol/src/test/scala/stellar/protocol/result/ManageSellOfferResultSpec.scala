package stellar.protocol.result

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.XdrSerdeMatchers
import stellar.protocol.ledger.LedgerEntryDatas

class ManageSellOfferResultSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import ManageSellOfferResults._

  "manage sell offer result" should {
    "serialise and deserialise" >> prop { result: ManageSellOfferResult =>
      result must xdrDecodeAndEncode(ManageSellOfferResult)
    }
  }
}

object ManageSellOfferResults {
  def genManageSellOfferCreated: Gen[ManageSellOfferCreated] = for {
    claims <- Gen.listOf(OfferClaims.genOfferClaim)
    entry <- LedgerEntryDatas.genOfferEntry
  } yield ManageSellOfferCreated(claims, entry)

  def genManageSellOfferUpdated: Gen[ManageSellOfferUpdated] = for {
    claims <- Gen.listOf(OfferClaims.genOfferClaim)
    entry <- LedgerEntryDatas.genOfferEntry
  } yield ManageSellOfferUpdated(claims, entry)

  def genManageSellOfferDeleted: Gen[ManageSellOfferDeleted] = for {
    claims <- Gen.listOf(OfferClaims.genOfferClaim)
  } yield ManageSellOfferDeleted(claims)

  def genSellManageOfferResult: Gen[ManageSellOfferResult] = Gen.oneOf(
    genManageSellOfferCreated,
    genManageSellOfferUpdated,
    genManageSellOfferDeleted,
    Gen.const(ManageSellOfferMalformed),
    Gen.const(ManageSellOfferBuyNoTrust),
    Gen.const(ManageSellOfferSellNoTrust),
    Gen.const(ManageSellOfferBuyNoAuth),
    Gen.const(ManageSellOfferSellNoAuth),
    Gen.const(ManageSellOfferBuyNoIssuer),
    Gen.const(ManageSellOfferSellNoIssuer),
    Gen.const(ManageSellOfferLineFull),
    Gen.const(ManageSellOfferUnderfunded),
    Gen.const(ManageSellOfferCrossSelf),
    Gen.const(ManageSellOfferLowReserve),
    Gen.const(UpdateSellOfferIdNotFound)
  )
  implicit val arbSellManageOfferResult: Arbitrary[ManageSellOfferResult] =
    Arbitrary(genSellManageOfferResult)
}
