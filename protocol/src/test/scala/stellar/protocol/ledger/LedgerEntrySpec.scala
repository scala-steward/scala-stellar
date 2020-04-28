package stellar.protocol.ledger

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.XdrSerdeMatchers

class LedgerEntrySpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import LedgerEntries._

  "ledger entry" should {
    "serialise and deserialise" >> prop { ledgerEntry: LedgerEntry =>
      ledgerEntry must xdrDecodeAndEncode(LedgerEntry)
    }
  }
}

object LedgerEntries {
  val genLedgerEntry: Gen[LedgerEntry] = for {
    lastModifiedLedgerSeq <- Gen.posNum[Int]
    (data, idx) <- LedgerEntryDatas.genLedgerEntryData
  } yield LedgerEntry(lastModifiedLedgerSeq, data, idx)

  implicit val arbLedgerEntry: Arbitrary[LedgerEntry] = Arbitrary(genLedgerEntry)
}