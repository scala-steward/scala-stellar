package stellar.protocol

import org.stellar.xdr.MuxedAccount.MuxedAccountMed25519
import org.stellar.xdr.{CryptoKeyType, MuxedAccount, Uint64}

/**
 * An Address is how accounts are identified.
 *
 * @param accountId The public component of an account on the network.
 * @param memoId    The optional memo id used to multiplex sub accounts at the same account ID.
 */
case class Address(
  accountId: AccountId,
  memoId: Option[Long] = None
) {
  def xdrEncodeMultiplexed: MuxedAccount = memoId match {
    case None => accountId.xdrEncodeMultiplexed
    case Some(id) => new MuxedAccount.Builder()
      .discriminant(CryptoKeyType.KEY_TYPE_MUXED_ED25519)
      .med25519(new MuxedAccountMed25519.Builder()
        .ed25519(accountId.xdrEncode.getAccountID.getEd25519)
        .id(new Uint64(id))
        .build())
      .build()
  }
}

object Address {
  /**
   * An Address is how accounts are identified.
   *
   * @param address The public component of an account on the network.
   */
  def apply(address: String): Address = {
    require(address.headOption.contains('G'))
    Address(AccountId(address))
  }

  /**
   * An Address is how accounts are identified.
   *
   * @param address The public component of an account on the network.
   * @param memoId  The memo id used to multiplex sub accounts at the same account ID.
   */
  def apply(address: String, memoId: Long): Address = {
    require(address.headOption.contains('G'))
    Address(AccountId(address), Some(memoId))
  }

  def decode(xdr: MuxedAccount): Address = {
    xdr.getDiscriminant match {
      case CryptoKeyType.KEY_TYPE_ED25519 => Address(AccountId.decode(xdr))
    }
  }
}