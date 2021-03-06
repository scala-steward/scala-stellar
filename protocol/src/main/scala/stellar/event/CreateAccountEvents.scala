package stellar.event

import org.stellar.xdr.CreateAccountResultCode.CREATE_ACCOUNT_SUCCESS
import org.stellar.xdr.{CreateAccountOp, MuxedAccount, Operation, OperationResult, OperationResultCode}
import stellar.protocol.AccountId


/**
 * Account was created.
 */
case class AccountCreated(
  accountId: AccountId,
  startingBalance: Long,
  fundingAccountId: AccountId // TODO - Address?
) extends CreateAccountEvent

object AccountCreated {

  def decode(op: CreateAccountOp, source: MuxedAccount): AccountCreated = {
    AccountCreated(
      accountId = AccountId.decode(op.getDestination.getAccountID),
      startingBalance = op.getStartingBalance.getInt64,
      fundingAccountId = AccountId.decode(source)
    )
  }
}
