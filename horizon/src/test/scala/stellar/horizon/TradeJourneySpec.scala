package stellar.horizon

import com.typesafe.scalalogging.LazyLogging
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import stellar.event.{AccountCreated, BidPlaced, TrustChanged}
import stellar.horizon.testing.TestAccountPool
import stellar.protocol.op.{CancelBid, PlaceBid, TrustAsset}
import stellar.protocol.{Amount, Lumen, Price, Seed, Token}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

class TradeJourneySpec(implicit ee: ExecutionEnv) extends Specification with LazyLogging {

  private val horizon = Horizon.async(Horizon.Networks.Test)
  private lazy val testAccountPool = Await.result(TestAccountPool.create(10), 1.minute)
  private lazy val asset = Token("wani", testAccountPool.borrow.accountId)

  "placing a bid" should {
    "be successful when matched" >> {
      pending
    }

    "be successful when unmatched" >> {
      val bidder = testAccountPool.borrow
      horizon.transact(bidder, List(
        TrustAsset(asset, 100_000),
        PlaceBid(
          selling = Lumen.stroops(100),
          buying = Amount(asset, 5_700)
        ))
      ) should beLike[TransactionResponse] { res =>
        res.operationEvents.head mustEqual TrustChanged(bidder.address, asset, 100_000)
        res.operationEvents(1) must beLike { case op: BidPlaced =>
          op.source mustEqual bidder.address
          op.id mustNotEqual 0
          op.selling mustEqual Lumen.stroops(100)
          op.buying mustEqual Amount(asset, 5_700)
          op.price mustEqual Price(57, 1)
        }
        res.feeCharged mustEqual 200L
        res.accepted must beTrue
      }.await(0, 10.seconds)
    }
  }

  "placing an ask" should {
    "be successful when matched" >> {
      pending
    }

    "be successful when unmatched" >> {
      pending
    }
  }

  // Close the accounts and return their funds back to friendbot
  step { logger.info("Ensuring all tests are complete before closing pool.") }
  step { Await.result(testAccountPool.close(), 10.minute) }
}
