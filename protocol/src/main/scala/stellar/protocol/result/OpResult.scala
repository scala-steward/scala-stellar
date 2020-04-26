package stellar.protocol.result

import stellar.protocol.xdr.Encodable

trait OpResult extends Encodable {
  val opCode: Int
}
