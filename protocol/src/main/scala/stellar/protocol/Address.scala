package stellar.protocol

import cats.data.State
import stellar.protocol.xdr.{Decoder, Encodable, Encode}

/**
 * An Address is how accounts are identified.
 * @param accountId The public component of an account on the network.
 * @param subAccountId A sub-identifier for the owner of the account to multiplex incoming payments.
 */
case class Address(accountId: AccountId, subAccountId: Option[Long] = None) extends Encodable {

  override def encode: LazyList[Byte] = subAccountId match {
    case Some(id) => Encode.int(0x100) ++ Encode.long(id) ++ accountId.encode
    case None => Encode.int(0x000) ++ accountId.encode
  }
}

object Address extends Decoder[Address] {
  override val decode: State[Seq[Byte], Address] = int.flatMap {
    case 0x000 => AccountId.decode.map(Address(_))
    case 0x100 => for {
      subId <- long
      accountId <- AccountId.decode
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