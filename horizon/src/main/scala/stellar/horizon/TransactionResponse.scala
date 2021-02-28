package stellar.horizon

import okio.ByteString
import org.stellar.xdr.CreateAccountResultCode.CREATE_ACCOUNT_SUCCESS
import org.stellar.xdr.OperationType.CREATE_ACCOUNT
import org.stellar.xdr._
import stellar.event.{AccountCreated, CreateAccountOpEvent, OperationEvent}
import stellar.protocol.{AccountId, Amount, Lumen}

case class TransactionResponse(
  operationEvents: List[OperationEvent],
  feeCharged: Amount
)


object TransactionResponse {
  def apply(
    hash: String,
    ledger: Long,
    envelopeXdr: String,
    resultXdr: String,
    resultMetaXdr: String
  ): TransactionResponse = {
    val result: TransactionResult = TransactionResult.decode(ByteString.decodeBase64(resultXdr))
    val envelope: TransactionEnvelope = TransactionEnvelope.decode(ByteString.decodeBase64(envelopeXdr))
    val feeCharged: Amount = Lumen.stroops(result.getFeeCharged.getInt64)
    val operationEvents: List[OperationEvent] = {
      val operationsRequested = (envelope.getDiscriminant match {
        // case EnvelopeType.ENVELOPE_TYPE_TX_V0 => envelope.getV0.getTx.getOperations
        case EnvelopeType.ENVELOPE_TYPE_TX => envelope.getV1.getTx.getOperations
        // case EnvelopeType.ENVELOPE_TYPE_TX_FEE_BUMP => envelope.getFeeBump.getTx.getInnerTx.getV1.getTx.getOperations
        case default => throw new IllegalStateException(s"Unexpected envelope type: $default")
      }).toList

      val rawResults = result.getResult.getResults.toList

      operationsRequested.zip(rawResults).map {
        case (requested, result) =>
          requested.getBody.getDiscriminant match {
            case CREATE_ACCOUNT => CreateAccountOpEvent.decode(requested, result)
            case default => throw new IllegalStateException(s"Unexpected operation type: $default")
          }
      }
    }

    TransactionResponse(
      operationEvents = operationEvents,
      feeCharged = feeCharged
    )
  }
}