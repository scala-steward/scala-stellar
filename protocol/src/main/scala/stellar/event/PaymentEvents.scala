package stellar.event

import org.stellar.xdr.{MuxedAccount, PaymentOp, PaymentResultCode}
import stellar.protocol.{Address, Amount}

/**
 * Payment was completed.
 */
case class PaymentMade(
  override val source: Address,
  to: Address,
  amount: Amount
) extends PaymentEvent {
  override val accepted: Boolean = true
}

object PaymentMade {
  def decode(
    op: PaymentOp,
    source: MuxedAccount
  ): PaymentMade = {
    PaymentMade(
      source = Address.decode(source),
      to = Address.decode(op.getDestination),
      amount = Amount.decode(op.getAsset, op.getAmount)
    )
  }
}

case class PaymentFailed(
  override val source: Address,
  to: Address,
  amount: Amount,
  failure: PaymentFailed.EnumVal
) extends PaymentEvent {
  override val accepted: Boolean = false
}

object PaymentFailed {
  sealed trait EnumVal
  /** The sender has insufficient funds to make this payment */
  case object InsufficientFunds extends EnumVal
  /** The sender or the recipient (or both) does not trust the asset being paid */
  case object MissingTrustLine extends EnumVal
  /** This payment would put the recipient's balance above the maximum trusted limit for this asset */
  case object OverTrustLimit extends EnumVal
  /** The recipient account does not exist */
  case object RecipientDoesNotExist extends EnumVal

  private val failureTypes = Map(
    PaymentResultCode.PAYMENT_LINE_FULL -> OverTrustLimit,
    PaymentResultCode.PAYMENT_NO_DESTINATION -> RecipientDoesNotExist,
    PaymentResultCode.PAYMENT_NO_TRUST -> MissingTrustLine,
    PaymentResultCode.PAYMENT_SRC_NO_TRUST -> MissingTrustLine,
    PaymentResultCode.PAYMENT_UNDERFUNDED -> InsufficientFunds,
  )

  def decode(
    op: PaymentOp,
    source: MuxedAccount,
    failure: PaymentResultCode
  ) = PaymentFailed(
      source = Address.decode(source),
      to = Address.decode(op.getDestination),
      amount = Amount.decode(op.getAsset, op.getAmount),
      failure = failureTypes(failure)
    )
}
