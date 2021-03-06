package stellar.event

import org.stellar.xdr.CreateAccountResultCode.CREATE_ACCOUNT_SUCCESS
import org.stellar.xdr.PaymentResultCode.PAYMENT_SUCCESS
import org.stellar.xdr.{MuxedAccount, Operation, OperationResult, OperationResultCode}

sealed trait OperationEvent

trait CreateAccountEvent extends OperationEvent
object CreateAccountEvent {
  def decode(
    requested: Operation,
    result: OperationResult,
    source: MuxedAccount
  ): CreateAccountEvent = {
    result.getDiscriminant match {
      case OperationResultCode.opINNER =>
        result.getTr.getCreateAccountResult.getDiscriminant match {
          case CREATE_ACCOUNT_SUCCESS => AccountCreated.decode(
            op = requested.getBody.getCreateAccountOp,
            source = Option(requested.getSourceAccount).getOrElse(source)
          )
        }
    }
  }
}

trait PaymentEvent extends OperationEvent
object PaymentEvent {
  def decode(
    requested: Operation,
    result: OperationResult,
    source: MuxedAccount
  ): PaymentEvent = {
    result.getDiscriminant match {
      case OperationResultCode.opINNER =>
        result.getTr.getPaymentResult.getDiscriminant match {
          case PAYMENT_SUCCESS => PaymentMade.decode(
            op = requested.getBody.getPaymentOp,
            source = Option(requested.getSourceAccount).getOrElse(source)
          )
        }
    }
  }
}
