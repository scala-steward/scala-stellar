package stellar.protocol

import cats.data.State
import okio.ByteString
import stellar.protocol.xdr.{Decode, Encodable, Encode}

case class PublicKey(bytes: ByteString) extends Encodable {
  override def encode: LazyList[Byte] = Encode.int(0) ++ Encode.bytes(32, bytes.toByteArray)
}

object PublicKey extends Decode {
  val decode: State[Seq[Byte], PublicKey] = for {
    _ <- int
    bs <- bytes(32)
  } yield PublicKey(new ByteString(bs.toArray))
}