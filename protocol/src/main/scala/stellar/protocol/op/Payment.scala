package stellar.protocol.op

import stellar.protocol.{Address, Amount}

/**
 * A payment operation.
 *
 * @param sender    the issuer of the funds. If absent, this issuer will be the source of the transaction.
 * @param recipient the receiver of the funds
 * @param amount    the amount to be sent
 */
case class Payment(sender: Option[Address], recipient: Address, amount: Amount) extends Operation
