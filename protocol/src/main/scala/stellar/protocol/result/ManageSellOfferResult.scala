package stellar.protocol.result

import cats.data.State
import stellar.protocol.ledger.OfferEntry
import stellar.protocol.xdr.Decoder
import stellar.protocol.xdr.Encode.{arr, int}

sealed abstract class ManageSellOfferResult(val opResultCode: Int) extends OpResult {
  override val opCode: Int = 3
  override def encode: LazyList[Byte] = int(opResultCode)
}

object ManageSellOfferResult extends Decoder[ManageSellOfferResult] {
  val decode: State[Seq[Byte], ManageSellOfferResult] = int.flatMap {
    case 0 => for {
      claims <- arr(OfferClaim.decode)
      result <- switch[ManageSellOfferResult](
        widen(OfferEntry.decode.map(entry => ManageSellOfferCreated(claims, entry))),
        widen(OfferEntry.decode.map(entry => ManageSellOfferUpdated(claims, entry))),
        widen(State.pure(ManageSellOfferDeleted(claims)))
      )
    } yield result
    case -1 => State.pure(ManageOfferMalformed)
    case -2 => State.pure(ManageOfferSellNoTrust)
    case -3 => State.pure(ManageOfferBuyNoTrust)
    case -4 => State.pure(ManageOfferSellNoAuth)
    case -5 => State.pure(ManageOfferBuyNoAuth)
    case -6 => State.pure(ManageOfferLineFull)
    case -7 => State.pure(ManageOfferUnderfunded)
    case -8 => State.pure(ManageOfferCrossSelf)
    case -9 => State.pure(ManageOfferSellNoIssuer)
    case -10 => State.pure(ManageOfferBuyNoIssuer)
    case -11 => State.pure(UpdateOfferIdNotFound)
    case -12 => State.pure(ManageOfferLowReserve)
  }
}

/**
 * ManageSellOffer operation was successful and an offer was created.
 *
 * @param claims the trades that were effected as a result of posting this offer.
 * @param entry the offer entry that was newly created.
 */
case class ManageSellOfferCreated(claims: List[OfferClaim], entry: OfferEntry) extends ManageSellOfferResult(0) {
  override def encode: LazyList[Byte] = super.encode ++ arr(claims) ++ int(0) ++ entry.encode
}

/**
 * ManageSellOffer operation was successful and an offer was updated.
 *
 * @param claims the trades that were effected as a result of posting this offer.
 * @param entry the offer entry that was newly updated.
 */
case class ManageSellOfferUpdated(claims: List[OfferClaim], entry: OfferEntry) extends ManageSellOfferResult(0) {
  override def encode: LazyList[Byte] = super.encode ++ arr(claims) ++ int(1) ++ entry.encode
}

/**
 * ManageSellOffer operation was successful and an offer was deleted.
 *
 * @param claims the trades that were effected as a result of posting this offer.
 */
case class ManageSellOfferDeleted(claims: List[OfferClaim]) extends ManageSellOfferResult(0) {
  override def encode: LazyList[Byte] = super.encode ++ arr(claims) ++ int(2)
}

/**
 * ManageOffer operation failed because the request was malformed.
 * E.g. Either of the assets were invalid, the assets were the same as each other,
 * the amount was less than zero, or the price numerator or denominator were zero or less.
 */
case object ManageOfferMalformed extends ManageSellOfferResult(-1)

/**
 * ManageOffer operation failed because there was no trustline for what was being offered.
 * (This also implies the account was underfunded).
 */
case object ManageOfferSellNoTrust extends ManageSellOfferResult(-2)

/**
 * ManageOffer operation failed because there was no trustline for what was being sought.
 */
case object ManageOfferBuyNoTrust extends ManageSellOfferResult(-3)

/**
 * ManageOffer operation failed because the account is not authorised to sell the offered asset.
 */
case object ManageOfferSellNoAuth extends ManageSellOfferResult(-4)

/**
 * ManageOffer operation failed because the account is not authorised to buy the sought asset.
 */
case object ManageOfferBuyNoAuth extends ManageSellOfferResult(-5)

/**
 * ManageOffer operation failed because it would have put the account's balance over the limit for the sought asset.
 */
case object ManageOfferLineFull extends ManageSellOfferResult(-6)

/**
 * ManageOffer operation failed because there was an insufficient balance of the asset being offered to meet the offer.
 */
case object ManageOfferUnderfunded extends ManageSellOfferResult(-7)

/**
 * ManageOffer operation failed because it would have matched with an offer from the same account.
 */
case object ManageOfferCrossSelf extends ManageSellOfferResult(-8)

/**
 * ManageOffer operation failed because there is no issuer for the asset being offered.
 */
case object ManageOfferSellNoIssuer extends ManageSellOfferResult(-9)

/**
 * ManageOffer operation failed because there is no issuer for the asset being sought.
 */
case object ManageOfferBuyNoIssuer extends ManageSellOfferResult(-10)

/**
 * ManageOffer operation failed because it was an update attempt, but an offer with the given id did not exist.
 */
case object UpdateOfferIdNotFound extends ManageSellOfferResult(-11)

/**
 * ManageOffer operation failed because the cumulative amount of it & all current offers from the same account
 * would exceed the account's available balance.
 */
case object ManageOfferLowReserve extends ManageSellOfferResult(-12)


