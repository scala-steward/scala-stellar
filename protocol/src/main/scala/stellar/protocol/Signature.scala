package stellar.protocol

import okio.ByteString
import org.stellar.xdr.{DecoratedSignature, SignatureHint}

case class Signature(data: ByteString, hint: ByteString) {

  def xdrEncode: DecoratedSignature = new DecoratedSignature.Builder()
    .hint(new SignatureHint(hint.toByteArray))
    .signature(new org.stellar.xdr.Signature(data.toByteArray))
    .build()

}