package stellar.protocol.op

import org.stellar.xdr
import org.stellar.xdr.Operation.OperationBody
import org.stellar.xdr.{Int64, ManageBuyOfferOp, OperationType}
import stellar.protocol.{Address, Amount, Asset, Price}

/**
 * Creates a new buy offer on the exchange.
 *
 * @param source    the issuer of the funds. If absent, the source of the transaction will be the source of funds
 */
case class PlaceBid(
  buying: Amount,
  selling: Amount,
  source: Option[Address] = None
) extends Operation {
  override def xdrEncode: xdr.Operation = new xdr.Operation.Builder()
    .body(new OperationBody.Builder()
      .discriminant(OperationType.MANAGE_BUY_OFFER)
      .manageBuyOfferOp(new ManageBuyOfferOp.Builder()
        .offerID(new Int64(0))
        .buyAmount(new Int64(buying.units))
        .buying(buying.asset.xdrEncode)
        .selling(selling.asset.xdrEncode)
        .price(Price.from(selling.units, buying.units).xdrEncode)
        .build())
      .build())
    .sourceAccount(source.map(_.xdrEncodeMultiplexed).orNull)
    .build()
}

case class CancelBid(
  id: Long,
  selling: Amount,
  buying: Amount,
  source: Option[Address] = None
) extends Operation {
  override def xdrEncode: xdr.Operation = new xdr.Operation.Builder()
    .body(new OperationBody.Builder()
      .discriminant(OperationType.MANAGE_BUY_OFFER)
      .manageBuyOfferOp(new ManageBuyOfferOp.Builder()
        .offerID(new Int64(id))
        .buyAmount(new Int64(0))
        .buying(buying.asset.xdrEncode)
        .selling(selling.asset.xdrEncode)
        .price(Price.from(buying.units, selling.units).xdrEncode)
        .build())
      .build())
    .sourceAccount(source.map(_.xdrEncodeMultiplexed).orNull)
    .build()
}