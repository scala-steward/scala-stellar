package stellar.protocol.op

import org.stellar.xdr
import org.stellar.xdr.Operation.OperationBody
import org.stellar.xdr.{CreateAccountOp, Int64, OperationType}
import stellar.protocol.{AccountId, Address}

/**
 * A create account operation.
 *
 * @see See [[https://developers.stellar.org/docs/start/list-of-operations/#create-account the official guide]] for
 *      details on this operation.
 * @param accountId       The id of the account to be created
 * @param startingBalance The starting balance in stroops to create the account with.
 * @param source          Optionally, a source account if it should be different to that in the containing Transaction.
 */
case class CreateAccount(
  accountId: AccountId,
  startingBalance: Long,
  source: Option[Address] = None
) extends Operation {
  override def xdrEncode: xdr.Operation = new xdr.Operation.Builder()
    .body(new OperationBody.Builder()
      .discriminant(OperationType.CREATE_ACCOUNT)
      .createAccountOp(new CreateAccountOp.Builder()
        .destination(accountId.xdrEncode)
        .startingBalance(new Int64(startingBalance))
        .build())
      .build())
    .sourceAccount(source.map(_.xdrEncodeMultiplexed).orNull)
    .build()
}
