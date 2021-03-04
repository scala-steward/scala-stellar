package stellar.protocol

import org.stellar.xdr.{CryptoKeyType, MuxedAccount}

/**
 * An Address is how accounts are identified.
 *
 * @param accountId The public component of an account on the network.
 */
case class Address(accountId: AccountId) {

  // TODO - support true multiplexed addresses
  def xdrEncodeMultiplexed: MuxedAccount = accountId.xdrEncodeMultiplexed
}

object Address {
  def apply(address: String): Address = {
    require(address.headOption.contains('G'))
    Address(AccountId(address))
  }
}