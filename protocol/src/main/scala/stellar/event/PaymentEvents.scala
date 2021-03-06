package stellar.event

import org.stellar.xdr.{MuxedAccount, Operation, OperationResult, OperationResultCode, PaymentOp}
import org.stellar.xdr.PaymentResultCode.PAYMENT_SUCCESS
import stellar.protocol.{AccountId, Address, Amount}

/**
 * Payment was completed.
 */
case class PaymentMade(
  from: AccountId,
  to: Address,
  amount: Amount
) extends PaymentEvent

object PaymentMade {
  def decode(
    op: PaymentOp,
    source: MuxedAccount
  ): PaymentMade = {
    PaymentMade(
      from = AccountId.decode(source),
      to = Address.decode(op.getDestination),
      amount = Amount.decode(op.getAsset, op.getAmount)
    )
  }
}