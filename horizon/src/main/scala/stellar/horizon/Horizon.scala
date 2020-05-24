package stellar.horizon

import stellar.protocol.AccountId

trait Horizon {
  def accountDetail(accountIdString: String): AccountId

}
