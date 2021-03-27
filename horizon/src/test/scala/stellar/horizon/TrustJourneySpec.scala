package stellar.horizon

import com.typesafe.scalalogging.LazyLogging
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import stellar.horizon.testing.TestAccountPool

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class TrustJourneySpec(implicit ee: ExecutionEnv) extends Specification with LazyLogging {

  private val horizon = Horizon.async(Horizon.Networks.Test)
  private lazy val testAccountPool = Await.result(TestAccountPool.create(15), 1.minute)

  "trusting an asset" should {
    "pending" >> pending
  }

}
