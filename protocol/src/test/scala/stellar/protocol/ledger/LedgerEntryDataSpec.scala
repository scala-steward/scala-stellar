package stellar.protocol.ledger

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.AccountIds.genAccountId
import stellar.protocol.{Signers, XdrSerdeMatchers}

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
    account <- genAccountId
    balance <- Gen.posNum[Long]
    seqNum <- Gen.posNum[Long]
    numSubEntries <- Gen.posNum[Int]
    inflationDestination <- Gen.option(genAccountId)
    flags <- IssuerFlags.genIssuerFlags
    homeDomain <- Gen.option(Gen.identifier)
    thresholds <- LedgerThresholds.genLedgerThreshold
    signers <- Gen.listOf(Signers.genSigner)
    liabilities <- Gen.option(LiabilitySums.genLiabilitySum)
  } yield AccountEntry(account, balance, seqNum, numSubEntries, inflationDestination, flags, homeDomain,
    thresholds, signers, liabilities)

  val genLedgerEntryData: Gen[LedgerEntryData] = Gen.oneOf(genAccountEntry, genAccountEntry)
  implicit val arbLedgerEntryData: Arbitrary[LedgerEntryData] = Arbitrary(genLedgerEntryData)
}
