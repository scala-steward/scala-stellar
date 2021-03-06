package stellar.protocol

import okio.ByteString
import org.stellar.xdr
import org.stellar.xdr.EnvelopeType.ENVELOPE_TYPE_TX
import org.stellar.xdr.TransactionSignaturePayload.TransactionSignaturePayloadTaggedTransaction
import org.stellar.xdr.{EnvelopeType, Int64, Memo, MemoType, SequenceNumber, TimeBounds, TimePoint, TransactionSignaturePayload, TransactionV1Envelope, Uint32, Uint64}
import stellar.protocol.op.Operation

/**
 * TODO
 */
case class Transaction(
  networkId: NetworkId,
  source: AccountId,
  sequence: Long,
  operations: List[Operation],
  maxFee: Int,
//  memo: Memo,
//  timeBounds: TimeBounds,
  signatures: Set[Signature] = Set.empty
) {

  /**
   * Adds signatures to this transaction.
   */
  def sign(signingKeys: SigningKey*): Transaction = {
    val payload = signatureBase.sha256()
    this.copy(signatures = this.signatures ++ signingKeys.map(_.sign(payload)).toSet)
  }

  /**
   * Encodes the underlying transaction without any signatures.
   * Further convert to base64 with `encodeUnsigned.base64()`
   */
//  def encodeUnsigned: ByteString = xdrEncode.encode()

  /**
   * Encodes the transaction, enveloped with signatures.
   * Further convert to base64 with `encodeSigned.base64()`
   */
  def encodeSigned: ByteString = xdrEncodeEnvelope.encode()

  def xdrEncode: xdr.Transaction = new xdr.Transaction.Builder()
    .sourceAccount(source.xdrEncodeMultiplexed)
    .fee(new Uint32(maxFee))
    .seqNum(new SequenceNumber(new Int64(sequence)))
    .memo(new Memo.Builder().discriminant(MemoType.MEMO_NONE).build())
    .timeBounds(new TimeBounds.Builder().minTime(new TimePoint(new Uint64(0L))).maxTime(new TimePoint(new Uint64(0L))).build())
    .operations(operations.map(_.xdrEncode).toArray)
    .ext(new xdr.Transaction.TransactionExt.Builder().discriminant(0).build())
    .build()

  private def xdrEncodeEnvelope: xdr.TransactionEnvelope = new xdr.TransactionEnvelope.Builder()
    .discriminant(ENVELOPE_TYPE_TX)
    .v1(new TransactionV1Envelope.Builder()
      .tx(xdrEncode)
      .signatures(signatures.map(_.xdrEncode).toArray)
      .build())
    .build()

  private def signatureBase: ByteString =
    new TransactionSignaturePayload.Builder()
      .networkId(networkId.xdrEncode)
      .taggedTransaction(new TransactionSignaturePayloadTaggedTransaction.Builder()
        .discriminant(EnvelopeType.ENVELOPE_TYPE_TX)
        .tx(xdrEncode)
        .build())
      .build()
      .encode()

}
