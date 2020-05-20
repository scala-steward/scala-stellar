package stellar.protocol.ledger

import cats.data.State
import stellar.protocol.xdr.Encode.int
import stellar.protocol.xdr.{Decoder, Encodable}

case class LedgerEntry(lastModifiedLedgerSeq: Int, data: LedgerEntryData, private val dataDisc: Int) extends Encodable {
  override def encode: LazyList[Byte] = int(lastModifiedLedgerSeq) ++ data.encodeDiscriminated
}

object LedgerEntry extends Decoder[LedgerEntry] {
  val decode: State[Seq[Byte], LedgerEntry] = for {
    lastModifiedLedgerSeq <- int
    dataDisc <- switchInt[LedgerEntryData](
      widen(AccountEntry.decode),
      widen(TrustLineEntry.decode),
      widen(OfferEntry.decode),
      widen(DataEntry.decode)
    )
    (data, disc) = dataDisc
  } yield LedgerEntry(lastModifiedLedgerSeq, data, disc)
}