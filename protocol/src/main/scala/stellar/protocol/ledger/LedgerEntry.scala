package stellar.protocol.ledger

import cats.data.State
import stellar.protocol.xdr.Encode.{arr, int, long, opt, string}
import stellar.protocol.xdr.{Decoder, Encodable, Encode}
import stellar.protocol.{AccountId, Asset, Signer, Token}

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

case class TrustLineEntry(account: AccountId, token: Token, balance: Long, limit: Long,
                          issuerAuthorized: Boolean, liabilities: Option[LiabilitySum])
  extends LedgerEntryData {

  override def encode: LazyList[Byte] =
    int(1) ++
      account.encode ++
      token.encode ++
      Encode.long(balance) ++
      Encode.long(limit) ++
      Encode.bool(issuerAuthorized) ++
      Encode.opt(liabilities)

}

object TrustLineEntry extends Decoder[TrustLineEntry] {
  val decode: State[Seq[Byte], TrustLineEntry] = for {
    account <- AccountId.decode
    asset <- Asset.decode.map(_.asInstanceOf[Token])
    balance <- long
    limit <- long
    issuerAuthorized <- bool
    liabilities <- opt(LiabilitySum.decode)
  } yield TrustLineEntry(account, asset, balance, limit, issuerAuthorized, liabilities)
}

object LedgerEntryData extends Decoder[LedgerEntryData] {
  override val decode: State[Seq[Byte], LedgerEntryData] = switch[LedgerEntryData](
      widen(AccountEntry.decode),
      widen(TrustLineEntry.decode),
//      widen(OfferEntry.decode),
//      widen(DataEntry.decode)
    )
}