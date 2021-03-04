package stellar.event

import org.stellar.xdr.CreateAccountResultCode.CREATE_ACCOUNT_SUCCESS
import org.stellar.xdr._
import stellar.protocol.AccountId

sealed trait OperationEvent

sealed trait CreateAccountOpEvent extends OperationEvent

object CreateAccountOpEvent {
  def decode(
    requested: Operation,
    result: OperationResult,
    source: MuxedAccount
  ): CreateAccountOpEvent = {
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


/**
 * Account was created.
 */
case class AccountCreated(
  accountId: AccountId,
  startingBalance: Long,
  fundingAccountId: AccountId
) extends CreateAccountOpEvent

object AccountCreated {

  def decode(op: CreateAccountOp, source: MuxedAccount): AccountCreated = {
    AccountCreated(
      accountId = AccountId.decode(op.getDestination.getAccountID),
      startingBalance = op.getStartingBalance.getInt64,
      fundingAccountId = AccountId.decode(source)
    )
  }
}

