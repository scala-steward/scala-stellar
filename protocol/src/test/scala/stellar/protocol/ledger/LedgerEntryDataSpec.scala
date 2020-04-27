package stellar.protocol.ledger

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.{AccountIds, Amounts, Assets, Prices, Signers, XdrSerdeMatchers}

class LedgerEntryDataSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import LedgerEntryDatas._

  "ledger entry data" should {
    "serialise and deserialise" >> prop { data: LedgerEntryData =>
      data must xdrDecodeAndEncode(LedgerEntryData)
    }
  }
}

object LedgerEntryDatas {
  val genAccountEntry: Gen[AccountEntry] = for {
    account <- AccountIds.genAccountId
    balance <- Gen.posNum[Long]
    seqNum <- Gen.posNum[Long]
    numSubEntries <- Gen.posNum[Int]
    inflationDestination <- Gen.option(AccountIds.genAccountId)
    flags <- IssuerFlags.genIssuerFlags
    homeDomain <- Gen.option(Gen.identifier)
    thresholds <- LedgerThresholds.genLedgerThreshold
    signers <- Gen.listOf(Signers.genSigner)
    liabilities <- Gen.option(LiabilitySums.genLiabilitySum)
  } yield AccountEntry(account, balance, seqNum, numSubEntries, inflationDestination, flags, homeDomain,
    thresholds, signers, liabilities)

  val genTrustLineEntry: Gen[TrustLineEntry] = for {
    account <- AccountIds.genAccountId
    asset <- Assets.genToken
    balance <- Gen.posNum[Long]
    limit <- Gen.posNum[Long]
    issuerAuthorized <- Gen.oneOf(true, false)
    liabilities <- Gen.option(LiabilitySums.genLiabilitySum)
  } yield TrustLineEntry(account, asset, balance, limit, issuerAuthorized, liabilities)

  val genOfferEntry: Gen[OfferEntry] = for {
    account <- AccountIds.genAccountId
    offerId <- Gen.posNum[Long]
    selling <- Amounts.genAmount
    buying <- Assets.genAsset
    price <- Prices.genPrice
  } yield OfferEntry(account, offerId, selling, buying, price)

  val genLedgerEntryData: Gen[LedgerEntryData] = Gen.oneOf(
    genAccountEntry,
    genTrustLineEntry,
    genOfferEntry,
  )
  implicit val arbLedgerEntryData: Arbitrary[LedgerEntryData] = Arbitrary(genLedgerEntryData)
}
