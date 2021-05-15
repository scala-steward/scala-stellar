package stellar.horizon

import stellar.protocol.{AccountId, Amount, Price}

/** An offer on the DEX */
case class Offer(
  id: Long,
  seller: AccountId,
  selling: Amount,
  buying: Amount,
  price: Price
)
