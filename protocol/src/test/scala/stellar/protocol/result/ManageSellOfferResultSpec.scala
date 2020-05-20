package stellar.protocol.result

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.XdrSerdeMatchers
import stellar.protocol.ledger.LedgerEntryDatas

class ManageSellOfferResultSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import ManageSellOfferResults._

  "create account result" should {
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
    Gen.const(ManageOfferMalformed),
    Gen.const(ManageOfferBuyNoTrust),
    Gen.const(ManageOfferSellNoTrust),
    Gen.const(ManageOfferBuyNoAuth),
    Gen.const(ManageOfferSellNoAuth),
    Gen.const(ManageOfferBuyNoIssuer),
    Gen.const(ManageOfferSellNoIssuer),
    Gen.const(ManageOfferLineFull),
    Gen.const(ManageOfferUnderfunded),
    Gen.const(ManageOfferCrossSelf),
    Gen.const(ManageOfferLowReserve),
    Gen.const(UpdateOfferIdNotFound)
  )
  implicit val arbSellManageOfferResult: Arbitrary[ManageSellOfferResult] =
    Arbitrary(genSellManageOfferResult)
}
