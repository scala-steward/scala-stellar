package stellar.protocol

import cats.data.State
import stellar.protocol.xdr.Encode.{bytes, int, long}
import stellar.protocol.xdr.{Decoder, Encodable}

/**
 * An Address is how accounts are identified.
 * @param accountId The public component of an account on the network.
 * @param subAccountId A sub-identifier for the owner of the account to multiplex incoming payments.
 */
case class Address(accountId: AccountId, subAccountId: Option[Long] = None) extends Encodable {

  override def encode: LazyList[Byte] = subAccountId match {
    case Some(id) => int(0x100) ++ long(id) ++ bytes(32, accountId.hash)
    case None => int(0x000) ++ bytes(32, accountId.hash)
  }
}

object Address extends Decoder[Address] {
  override val decode: State[Seq[Byte], Address] = int.flatMap {
    case 0x000 => byteString(32).map(bs => Address(AccountId(bs)))
    case 0x100 => for {
      subId <- long
      accountId <- byteString(32).map(AccountId(_))
    } yield Address(accountId, Some(subId))
  }

/* TODO
  def apply(address: String): Address = address.headOption match {
    case Some('G') => Address(AccountId(address))
    case Some('M') =>
    case _ =>
  }
*/
}