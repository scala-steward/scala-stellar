package stellar.horizon

import okhttp3.Response
import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, Formats}
import stellar.horizon.json.AccountDetailReader
import stellar.protocol.AccountId

import scala.concurrent.Future
import scala.util.Try

/**
 * Operations related to Horizon endpoints for accounts.
 * @tparam F the effect type.
 */
trait AccountOperations[F[_]] {
  def get(path: String, params: Map[String, String] = Map.empty): F[Response]

  implicit protected val formats: Formats = DefaultFormats + AccountDetailReader

  def accountDetail(accountId: AccountId): F[AccountDetail]
  protected def accountDetailResponse(accountId: AccountId): F[Response] =
    get(s"accounts/${accountId.encodeToString}")

}

/**
 * Account operations effected by Scala Try.
 */
trait AccountOperationsSyncInterpreter extends AccountOperations[Try] {
  override def accountDetail(accountId: AccountId): Try[AccountDetail] =
    accountDetailResponse(accountId).map(response => parse(response.body().string()).extract[AccountDetail])
}

/**
 * Account operations effected by Scala Future.
 */
trait AccountOperationsAsyncInterpreter extends AccountOperations[Future] {
  import scala.concurrent.ExecutionContext.Implicits.global

  override def accountDetail(accountId: AccountId): Future[AccountDetail] =
    accountDetailResponse(accountId).map(response => parse(response.body().string()).extract[AccountDetail])
}
