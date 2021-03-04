package stellar.protocol

import okio.ByteString
import org.stellar.xdr.Hash

case class NetworkId(passphrase: String) {
  def xdrEncode: Hash = new Hash(ByteString.encodeUtf8(passphrase).sha256().toByteArray)
}
