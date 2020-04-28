package stellar.protocol.ledger

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.XdrSerdeMatchers
import stellar.protocol.ledger.LedgerEntries.genLedgerEntry
import stellar.protocol.ledger.LedgerKeys.genLedgerKey

class LedgerEntryChangeSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import LedgerEntryChanges._

  "ledger entry change" should {
    "serialise and deserialise" >> prop { change: LedgerEntryChange =>
      change must xdrDecodeAndEncode(LedgerEntryChange)
    }
  }
}

object LedgerEntryChanges {
  val genLedgerEntryCreate: Gen[LedgerEntryCreate] = genLedgerEntry.map(LedgerEntryCreate)
  val genLedgerEntryUpdate: Gen[LedgerEntryUpdate] = genLedgerEntry.map(LedgerEntryUpdate)
  val genLedgerEntryDelete: Gen[LedgerEntryDelete] = genLedgerKey.map(LedgerEntryDelete)
  val genLedgerEntryState: Gen[LedgerEntryState] = genLedgerEntry.map(LedgerEntryState)

  val genLedgerEntryChange: Gen[LedgerEntryChange] =
    Gen.oneOf(genLedgerEntryCreate, genLedgerEntryUpdate, genLedgerEntryDelete, genLedgerEntryState)

  implicit val arbLedgerEntryChange: Arbitrary[LedgerEntryChange] = Arbitrary(genLedgerEntryChange)
}
