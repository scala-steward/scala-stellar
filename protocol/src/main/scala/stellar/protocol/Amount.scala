package stellar.protocol

import org.stellar.xdr
import org.stellar.xdr.{Int64}

case class Amount(asset: Asset, units: Long)

object Amount {
  def decode(xdrAsset: xdr.Asset, xdrAmount: Int64): Amount =
    Amount(Asset.decode(xdrAsset), xdrAmount.getInt64)

}