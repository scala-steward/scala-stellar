package stellar.protocol

import okio.ByteString

case class Signature(data: ByteString, hint: ByteString)