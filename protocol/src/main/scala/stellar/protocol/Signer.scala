package stellar.protocol

import cats.data.State
import stellar.protocol.xdr.{Decoder, Encodable}
import stellar.protocol.xdr.Encode.int

case class Signer(key: SignerKey, weight: Int) extends Encodable {
  override def encode: LazyList[Byte] = key.encode ++ int(weight)
}

object Signer extends Decoder[Signer] {
  val decode: State[Seq[Byte], Signer] = for {
    key <- SignerKey.decode
    weight <- int
  } yield Signer(key, weight)
}
