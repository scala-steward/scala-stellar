package stellar.protocol

import cats.data.State
import stellar.protocol.xdr.{Decoder, Encodable, Encode}

case class Amount(asset: Asset, units: Long) extends Encodable {
  def encode: LazyList[Byte] = asset.encode ++ Encode.long(units)
}

object Amount extends Decoder[Amount] {
  val decode: State[Seq[Byte], Amount] = for {
    asset <- Asset.decode
    units <- long
  } yield Amount(asset, units)
}
