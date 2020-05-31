package stellar.horizon

import okhttp3.HttpUrl
import org.specs2.ScalaCheck
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import stellar.horizon.io.FakeHttpExchange
import stellar.horizon.json.AccountDetails

import scala.concurrent.Future
import scala.util.Try

class AccountOperationsSpec(implicit env: ExecutionEnv) extends Specification with ScalaCheck {
  import AccountDetails._

  private val baseUrl = HttpUrl.parse("http://localhost/")

  "account operation blocking interpreter" should {
    "fetch account details by account id" >> prop { accountDetail: AccountDetail =>
      val horizon = new Horizon[Try](baseUrl) with FakeHttpExchange[Try] with AccountOperationsSyncInterpreter
      horizon.respondWith(asJsonDoc(accountDetail), Try(_))
      horizon.accountDetail(accountDetail.id) must beSuccessfulTry(accountDetail)
      horizon.requestsMade.map(_.url.toString) mustEqual List(s"http://localhost/accounts/${accountDetail.id.encodeToString}")
    }
  }

  "account operation async interpreter" should {
    "fetch account details by account id" >> prop { accountDetail: AccountDetail =>
      val horizon = new Horizon[Future](baseUrl) with FakeHttpExchange[Future] with AccountOperationsAsyncInterpreter
      horizon.respondWith(asJsonDoc(accountDetail), Future(_))
      horizon.accountDetail(accountDetail.id) must beEqualTo(accountDetail).await
      horizon.requestsMade.map(_.url.toString) mustEqual List(s"http://localhost/accounts/${accountDetail.id.encodeToString}")
    }
  }
}
