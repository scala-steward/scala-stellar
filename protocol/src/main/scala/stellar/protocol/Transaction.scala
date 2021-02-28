package stellar.protocol

import cats.data.NonEmptyList
import stellar.protocol.op.Operation

/**
 * TODO
 */
case class Transaction(
  network: NetworkId,
  source: AccountId,
  sequence: Long,
  operations: NonEmptyList[Operation],
  maxFee: Int,
  keys: List[SigningKey] // TODO - this typing will hurt when decoding.
)