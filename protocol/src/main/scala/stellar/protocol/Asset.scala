package stellar.protocol

sealed trait Asset {
  val code: String

  // def apply(units: Long): Amount = Amount(this, units)
}

/**
 * The network's native asset, XLM.
 */
case object Lumen extends Asset {
  val STROOPS_PER_LUMEN = 10_000_000L

  override val code: String = "XLM"
  def apply(lumen: Int) = stroops(lumen * STROOPS_PER_LUMEN)
  def stroops(stroops: Long): Amount = Amount(Lumen, stroops)
}

/**
 * An account-defined custom asset.
 */
case class Token(code: String, issuer: AccountId) extends Asset {
  require(code.length >= 1 && code.length <= 12)
}
