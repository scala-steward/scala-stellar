package stellar.protocol

import cats.data.State
import stellar.protocol.xdr.{Decode, Encodable, Encode}

case class Amount(asset: Asset, units: Long) extends Encodable {
  def encode: LazyList[Byte] = asset.encode ++ Encode.long(units)
}

object Amount extends Decode {
  val decode: State[Seq[Byte], Amount] = for {
    asset <- Asset.decode
    units <- long
  } yield Amount(asset, units)
}
