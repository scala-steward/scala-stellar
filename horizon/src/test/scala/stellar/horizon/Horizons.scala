package stellar.horizon

import okhttp3.HttpUrl
import stellar.horizon.io.{HttpExchangeAsync, HttpExchangeSync}

import scala.concurrent.Future
import scala.util.Try

/**
 * A collection of well-known Horizon instances for convenience.
 * For any serious commercial use, you should stand-up your own Stellar node and Horizon instance in order to take full
 * advantage of the decentralized network.
 */
object Horizons {

  private val mainNetUrl = HttpUrl.parse("https://horizon.stellar.org/")
  private val testNetUrl = HttpUrl.parse("https://horizon-testnet.stellar.org/")

  object SdfTestNet {
    object Async extends Horizon[Future](testNetUrl) with HttpExchangeAsync with AccountOperationsAsyncInterpreter
    object Blocking extends Horizon[Try](testNetUrl) with HttpExchangeSync with AccountOperationsSyncInterpreter
  }

  object SdfMainNet {
    object Async extends Horizon[Future](mainNetUrl) with HttpExchangeAsync with AccountOperationsAsyncInterpreter
    object Blocking extends Horizon[Try](mainNetUrl) with HttpExchangeSync with AccountOperationsSyncInterpreter
  }
}
