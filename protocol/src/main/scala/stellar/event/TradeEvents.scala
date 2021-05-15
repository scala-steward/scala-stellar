package stellar.event

import org.stellar.xdr.{Int64, MuxedAccount, OfferEntry}
import stellar.protocol.op.{CancelBid, Operation}
import stellar.protocol.{Address, Amount, Asset, Price}

/**
 * Bid offer was placed
 */
case class BidPlaced(
  override val source: Address,
  id: Long,
  selling: Amount,
  buying: Amount,
  price: Price
) extends ManageBuyOfferEvent {
  def cancel: CancelBid = CancelBid(id, selling, buying, Some(source)) // FIXME - Is this OK? Useful?

  override val accepted: Boolean = true
}

object BidPlaced {
  def decode(
    source: MuxedAccount,
    offer: OfferEntry
  ): BidPlaced = {
    val price = Price.decode(offer.getPrice)
    BidPlaced(
      source = Address.decode(source),
      id = offer.getOfferID.getInt64,
      selling = Amount(Asset.decode(offer.getSelling), offer.getAmount.getInt64),
      buying = Amount(Asset.decode(offer.getBuying), price.times(offer.getAmount.getInt64)),
      price = price
    )
  }
}

/**
 * Bid offer was cancelled
 */
case class BidCancelled(
  override val source: Address,

) extends ManageBuyOfferEvent {
  override val accepted: Boolean = true
}

object BidCancelled {
  def decode(source: MuxedAccount, offer: OfferEntry): ManageBuyOfferEvent =
    BidCancelled(
      source = Address.decode(source)
    )
}