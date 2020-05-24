package stellar.horizon.testing

import stellar.horizon.{AccountDetail, Horizon}
import stellar.protocol.AccountId

case class FakeHorizon(data: AccountDetail) extends Horizon {

  override def accountDetail(accountIdString: String): AccountId = ???

}