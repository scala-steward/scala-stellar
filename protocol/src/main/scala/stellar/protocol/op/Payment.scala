package stellar.protocol.op

import org.stellar.xdr
import org.stellar.xdr.Operation.OperationBody
import org.stellar.xdr.{Int64, OperationType, PaymentOp}
import stellar.protocol.{Address, Amount}

/**
 * A payment operation.
 *
 * @param recipient the receiver of the funds
 * @param amount    the amount to be sent
 * @param source    the issuer of the funds. If absent, the source of the transaction with be the source of funds
 */
case class Payment(
  recipient: Address,
  amount: Amount,
  source: Option[Address] = None
) extends Operation {
  override def xdrEncode: xdr.Operation = ???
/* // Good, but untested
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
*/

}
