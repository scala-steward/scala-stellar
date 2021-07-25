package stellar.event
import org.stellar.xdr.{ChangeTrustOp, ChangeTrustResultCode, MuxedAccount}
import stellar.protocol.{Address, Token}

case class TrustChanged(
  override val source: Address,
  token: Token,
  limit: Long
) extends TrustChangeEvent {
  override val accepted: Boolean = true
}

case class TrustRemoved(
  override val source: Address,
  token: Token,
) extends TrustChangeEvent {
  override val accepted: Boolean = true
}

object TrustChanged {
  def decode(
    op: ChangeTrustOp,
    source: MuxedAccount
  ): TrustChangeEvent =
    if (op.getLimit.getInt64 == 0)
      TrustRemoved(
        source = Address.decode(source),
        token = Token.decode(op.getLine),
      )
    else TrustChanged(
      source = Address.decode(source),
      token = Token.decode(op.getLine),
      limit = op.getLimit.getInt64
    )
}

case class TrustChangeFailed(
  override val source: Address,
  failure: TrustChangeFailed.EnumVal
) extends TrustChangeEvent {
  override val accepted: Boolean = false
}

object TrustChangeFailed {
  sealed trait EnumVal
  /** Attempted to establish a trustline, but the asset issuing account does not exist */
  case object IssuerDoesNotExist extends EnumVal
  /** Attempted to remove a trustline, but it did not exist, or it does exist, but the balance is not zero */
  case object CannotRemoveTrustLine extends EnumVal

  private val failureTypes: Map[ChangeTrustResultCode, Long => TrustChangeFailed.EnumVal] = Map(
    ChangeTrustResultCode.CHANGE_TRUST_NO_ISSUER -> { _ => IssuerDoesNotExist },
    ChangeTrustResultCode.CHANGE_TRUST_INVALID_LIMIT -> { (limit: Long) =>
      if (limit == 0) CannotRemoveTrustLine else ???
    }
  )

  def decode(
    op: ChangeTrustOp,
    source: MuxedAccount,
    failure: ChangeTrustResultCode
  ): TrustChangeEvent = TrustChangeFailed(
    source = Address.decode(source),
    failure = failureTypes(failure).apply(op.getLimit.getInt64)
  )
}
