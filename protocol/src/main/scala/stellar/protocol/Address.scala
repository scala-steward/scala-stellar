package stellar.protocol

/**
 * An Address is how accounts are identified.
 *
 * @param accountId The public component of an account on the network.
 */
case class Address(accountId: AccountId)

object Address {
  def apply(address: String): Address = {
    require(address.headOption.contains('G'))
    Address(AccountId(address))
  }
}