package stellar.horizon

import com.typesafe.scalalogging.LazyLogging
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import stellar.event.{OperationEvent, PaymentFailed}
import stellar.event.PaymentFailed.{InsufficientFunds, RecipientDoesNotExist}
import stellar.horizon.ValidationResult.{SourceAccountDoesNotExist, Valid}
import stellar.horizon.testing.TestAccountPool
import stellar.protocol.op.Pay
import stellar.protocol.{Lumen, Seed, Transaction}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class PaymentJourneySpec(implicit ee: ExecutionEnv) extends Specification with LazyLogging {

  private val horizon = Horizon.async(Horizon.Networks.Test)
  private lazy val testAccountPool = Await.result(TestAccountPool.create(15), 1.minute)

  "transacting a payment" should {
    "fail when the lumen funds are insufficient" >> {
      val (from, to) = testAccountPool.borrowPair
      (for {
        fromAccountDetails <- horizon.account.detail(from.accountId)
        response <- horizon.transact(Transaction(
          networkId = horizon.networkId,
          source = from.accountId,
          sequence = fromAccountDetails.nextSequence,
          operations = List(
            Pay(to.address, Lumen(11_000))
          ),
          maxFee = 100
        ).sign(from))
      } yield response) must beLike[TransactionResponse] { res =>
        res.accepted must beFalse
        res.operationEvents mustEqual List(
          PaymentFailed(
            source = from.address,
            to = to.address,
            amount = Lumen(11_000),
            failure = InsufficientFunds
          )
        )
        res.feeCharged mustEqual 100L
        res.validationResult mustEqual Valid
      }.await(0, 10.seconds)
    }

    "fail when the sender does not exist" >> {
      val from = Seed.random
      val to = testAccountPool.borrow
      (for {
        toAccountDetails <- horizon.account.detail(to.accountId)
        response <- horizon.transact(Transaction(
          networkId = horizon.networkId,
          source = from.accountId,
          sequence = toAccountDetails.nextSequence,
          operations = List(
            Pay(to.address, Lumen(42))
          ),
          maxFee = 100
        ).sign(from, to))
      } yield response) must beLike[TransactionResponse] { res =>
        res.accepted must beFalse
        res.operationEvents must beEmpty[List[OperationEvent]]
        res.feeCharged mustEqual 0L
        res.validationResult mustEqual SourceAccountDoesNotExist
      }.await(0, 10.seconds)
    }

    "fail when the recipient does not exist" >> {
      val from = testAccountPool.borrow
      val to = Seed.random
      (for {
        fromAccountDetails <- horizon.account.detail(from.accountId)
        response <- horizon.transact(Transaction(
          networkId = horizon.networkId,
          source = from.accountId,
          sequence = fromAccountDetails.nextSequence,
          operations = List(
            Pay(to.address, Lumen(42))
          ),
          maxFee = 100
        ).sign(from))
      } yield response) must beLike[TransactionResponse] { res =>
        res.accepted must beFalse
        res.operationEvents mustEqual List(
          PaymentFailed(
            source = from.address,
            to = to.address,
            amount = Lumen(42),
            failure = RecipientDoesNotExist
          )
        )
        res.feeCharged mustEqual 100L
        res.validationResult mustEqual Valid
      }.await(0, 10.seconds)
    }

    "fail when the token funds are insufficient" >> pending("support for trustline creation")
    "fail when the recipient would go over their limit" >> pending("support for trustline creation")
    "fail when the asset is not trusted by the sender" >> pending("support for trustline creation")
    "fail when the asset is not trusted by the recipient" >> pending("support for trustline creation")
    "fail when the sender is not authorised to send this asset" >> pending("support for trustline creation")
    "fail when the recipient is not authorised to trust this asset" >> pending("support for trustline creation")
  }

  // Close the accounts and return their funds back to friendbot
  step { logger.info("Ensuring all tests are complete before closing pool.") }
  step { Await.result(testAccountPool.close(), 10.minute) }

}
