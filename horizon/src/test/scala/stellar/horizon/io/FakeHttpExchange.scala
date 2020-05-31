package stellar.horizon.io

import okhttp3._

/**
 * For testing, declare the behaviour when invocations are made and record the requests.
 * @tparam F the effect to return the response in
 */
trait FakeHttpExchange[F[_]] extends HttpExchange[F] {
  private val json = MediaType.get("application/json; charset=utf-8")

  var response: Option[Request => F[Response]] = None
  var requestsMade: List[Request] = Nil
  override def invoke(request: Request): F[Response] = {
    requestsMade :+= request
    response.map(_.apply(request)).get
  }

  def respondWith(text: String, effect: Response => F[Response]): Unit = {
    response = Some((request: Request) => effect(new Response.Builder()
      .body(ResponseBody.create(text, json))
      .code(200)
      .request(request)
      .protocol(Protocol.HTTP_2)
      .message("OK")
      .build))
  }
}
