package stellar.protocol.op

import org.stellar.xdr
import org.stellar.xdr.Operation.OperationBody
import org.stellar.xdr.{Int64, OperationType, PaymentOp}
import stellar.protocol.{Address, Amount}

/**
 * A payment operation.
 *
 * @see See [[https://developers.stellar.org/docs/start/list-of-operations/#payment the official guide]] for
 *      details on this operation.
 * @param recipient the receiver of the funds
 * @param amount    the amount to be sent
 * @param source    the issuer of the funds. If absent, the source of the transaction with be the source of funds
 */
case class Pay(
  recipient: Address,
  amount: Amount,
  source: Option[Address] = None
) extends Operation {
  override def xdrEncode: xdr.Operation = new xdr.Operation.Builder()
    .body(new OperationBody.Builder()
      .discriminant(OperationType.PAYMENT)
      .paymentOp(new PaymentOp.Builder()
        .amount(new Int64(amount.units))
        .asset(amount.asset.xdrEncode)
        .destination(recipient.xdrEncodeMultiplexed)
        .build())
      .build())
    .sourceAccount(source.map(_.xdrEncodeMultiplexed).orNull)
    .build()
}
