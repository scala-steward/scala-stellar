package stellar.protocol.ledger

import cats.data.State
import stellar.protocol.xdr.Encode.{arr, int, long, opt, string}
import stellar.protocol.xdr.{Decoder, Encodable, Encode}
import stellar.protocol.{AccountId, Signer}

sealed trait LedgerEntryData extends Encodable

case class AccountEntry(account: AccountId, balance: Long, seqNum: Long, numSubEntries: Int,
                        inflationDestination: Option[AccountId], flags: Set[IssuerFlag],
                        homeDomain: Option[String], thresholds: LedgerThreshold, signers: Seq[Signer],
                        liabilities: Option[LiabilitySum]) extends LedgerEntryData {

  override def encode: LazyList[Byte] =
    int(0) ++
      account.encode ++
      long(balance) ++
      long(seqNum) ++
      int(numSubEntries) ++
      opt(inflationDestination) ++
      int(flags.map(_.i + 0).fold(0)(_ | _)) ++
      string(homeDomain.getOrElse("")) ++
      thresholds.encode ++
      arr(signers) ++
      opt(liabilities)

}

object AccountEntry extends Decoder[AccountEntry] {
  val decode: State[Seq[Byte], AccountEntry] = for {
    account <- AccountId.decode
    balance <- long
    seqNum <- long
    numSubEntries <- int
    inflationDestination <- opt(AccountId.decode)
    flags <- IssuerFlagSet.decode
    homeDomain <- string.map(Some(_).filter(_.nonEmpty))
    thresholds <- LedgerThreshold.decode
    signers <- arr(Signer.decode)
    liabilities <- opt(LiabilitySum.decode)
  } yield AccountEntry(account, balance, seqNum, numSubEntries, inflationDestination, flags,
    homeDomain, thresholds, signers, liabilities)
}

object LedgerEntryData extends Decoder[LedgerEntryData] {
  override val decode: State[Seq[Byte], LedgerEntryData] = switch[LedgerEntryData](
      widen(AccountEntry.decode),
//      widen(TrustLineEntry.decode),
//      widen(OfferEntry.decode),
//      widen(DataEntry.decode)
    )
}