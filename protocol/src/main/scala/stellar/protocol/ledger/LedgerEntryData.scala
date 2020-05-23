package stellar.protocol.ledger

import cats.data.State
import okio.ByteString
import stellar.protocol._
import stellar.protocol.xdr.Encode._
import stellar.protocol.xdr.{Decoder, Encodable}

sealed abstract class LedgerEntryData(val discriminator: Int) extends Encodable {
  override def encodeDiscriminated: LazyList[Byte] = int(discriminator) ++ encode
}

case class AccountEntry(account: AccountId, balance: Long, seqNum: Long, numSubEntries: Int,
                        inflationDestination: Option[AccountId], flags: Set[IssuerFlag],
                        homeDomain: Option[String], thresholds: LedgerThreshold, signers: List[Signer],
                        liabilities: Option[LiabilitySum]) extends LedgerEntryData(0) {
  override def encode: LazyList[Byte] =
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
  extends LedgerEntryData(1) {

  override def encode: LazyList[Byte] =
      account.encode ++
      token.encode ++
      long(balance) ++
      long(limit) ++
      bool(issuerAuthorized) ++
      opt(liabilities)

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

case class OfferEntry(account: AccountId, offerId: Long, selling: Amount, buying: Asset, price: Price)
  extends LedgerEntryData(2) {

  override def encode: LazyList[Byte] =
      account.encode ++
      long(offerId) ++
      selling.asset.encode ++
      buying.encode ++
      long(selling.units) ++
      price.encode ++
      long(0)

}

object OfferEntry extends Decoder[OfferEntry] {
  val decode: State[Seq[Byte], OfferEntry] = for {
    account <- AccountId.decode
    offerId <- long
    selling <- Asset.decode
    buying <- Asset.decode
    units <- long
    price <- Price.decode
    _ <- int // flags
    _ <- int // ext
  } yield OfferEntry(account, offerId, Amount(selling, units), buying, price)
}

case class DataEntry(account: AccountId, name: String, value: ByteString) extends LedgerEntryData(3) {

  override def encode: LazyList[Byte] =
      account.encode ++
      string(name) ++
      padded(value.toByteArray) ++
      int(0)
}

object DataEntry extends Decoder[DataEntry] {
  val decode: State[Seq[Byte], DataEntry] = for {
    account <- AccountId.decode
    name <- string
    value <- padded()
    _ <- int
  } yield DataEntry(account, name, new ByteString(value.toArray))
}

object LedgerEntryData extends Decoder[LedgerEntryData] {
  override val decode: State[Seq[Byte], LedgerEntryData] = switch[LedgerEntryData](
      widen(AccountEntry.decode),
      widen(TrustLineEntry.decode),
      widen(OfferEntry.decode),
      widen(DataEntry.decode)
    )
}