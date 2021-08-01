package stellar.horizon

import com.typesafe.scalalogging.LazyLogging
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import stellar.event.PaymentFailed.{InsufficientFunds, InvalidAmount, MissingTrustLine, OverTrustLimit, RecipientDoesNotExist}
import stellar.event.{OperationEvent, PaymentFailed}
import stellar.horizon.ValidationResult.{SourceAccountDoesNotExist, Valid}
import stellar.horizon.testing.TestAccountPool
import stellar.protocol._
import stellar.protocol.op.{Pay, TrustAsset}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class PaymentJourneySpec(implicit ee: ExecutionEnv) extends Specification with LazyLogging {

  private val horizon = Horizon.async(Horizon.Networks.Test)
  private lazy val testAccountPool = Await.result(TestAccountPool.create(20), 1.minute)

  "transacting a single-asset payment" should {
    "fail when the lumen funds are insufficient" >> {
      val (from, to) = testAccountPool.borrowPair
      val response = horizon.transact(from, List(Pay(to.address, Lumen(11_000))))
      response must beLike[TransactionResponse] { res =>
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
      }.await(0, 30.seconds)
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
      }.await(0, 30.seconds)
    }

    "fail when the recipient does not exist" >> {
      val from = testAccountPool.borrow
      val to = Seed.random
      horizon.transact(from, List(Pay(to.address, Lumen(42)))) must beLike[TransactionResponse] { res =>
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
      }.await(0, 30.seconds)
    }

    "fail when the token funds are insufficient" >> {
      val (from, to) = testAccountPool.borrowPair
      val asset = Token("KOUGIRA", to.accountId)
      val response = for {
        _ <- horizon.transact(from, List(TrustAsset(asset, 100L)))
        _ <- horizon.transact(to, List(Pay(from.address, Amount(asset, 42L))))
        r <- horizon.transact(from, List(Pay(to.address, Amount(asset, 43L))))
      } yield r
      response must beLike[TransactionResponse] { res =>
        res.accepted must beFalse
        res.operationEvents mustEqual List(
          PaymentFailed(
            source = from.address,
            to = to.address,
            amount = Amount(asset, 43L),
            failure = InsufficientFunds
          )
        )
        res.feeCharged mustEqual 100L
        res.validationResult mustEqual Valid
      }.await(0, 30.seconds)
    }

    "fail when the recipient would go over their limit" >> {
      val (to, from) = testAccountPool.borrowPair
      val asset = Token("kaimono", from.accountId)
      val response = for {
        _ <- horizon.transact(to, List(TrustAsset(asset, 100L)))
        r <- horizon.transact(from, List(Pay(to.address, Amount(asset, 101L))))
      } yield r
      response must beLike[TransactionResponse] { res =>
        res.accepted must beFalse
        res.operationEvents mustEqual List(
          PaymentFailed(
            source = from.address,
            to = to.address,
            amount = Amount(asset, 101L),
            failure = OverTrustLimit
          )
        )
        res.feeCharged mustEqual 100L
        res.validationResult mustEqual Valid
      }.await(0, 30.seconds)
    }

    "fail when the asset is not trusted by the sender" >> {
      val (to, from) = testAccountPool.borrowPair
      val asset = Token("tanuki", testAccountPool.borrow.accountId)
      val response = for {
        x <- horizon.transact(to, List(TrustAsset(asset, 100L)))
        r <- horizon.transact(from, List(Pay(to.address, Amount(asset, 7))))
      } yield (r, x)
      response.map(_._1) must beLike[TransactionResponse] { res =>
        res.accepted must beFalse
        res.operationEvents mustEqual List(
          PaymentFailed(
            source = from.address,
            to = to.address,
            amount = Amount(asset, 7L),
            failure = MissingTrustLine
          )
        )
        res.feeCharged mustEqual 100L
        res.validationResult mustEqual Valid
      }.await(0, 30.seconds)
    }

    "fail when the asset is not trusted by the recipient" >> {
      val (to, from, issuer) = testAccountPool.borrowTriple
      val asset = Token("buta", issuer.accountId)
      val response = horizon.transact(from, List(Pay(to.address, Amount(asset, 7))))
      response must beLike[TransactionResponse] { res =>
        res.accepted must beFalse
        res.operationEvents mustEqual List(
          PaymentFailed(
            source = from.address,
            to = to.address,
            amount = Amount(asset, 7L),
            failure = MissingTrustLine
          )
        )
        res.feeCharged mustEqual 100L
        res.validationResult mustEqual Valid
      }.await(0, 30.seconds)
    }

    /** TODO: Build out the support for trustline modifications.
     *
     * These two tests seem to require the ability to issue SetTrustlineFlag operations.
     * Is it the case that the flags are:
     * 1. They can hold but not transaction
     * 2. They can hold and make offers, but not payments
     * 4. They can do anything but clawbacks are not possible *
    
    "fail when the sender is not authorised to send this asset" >> {
      val (to, from, issuer) = testAccountPool.borrowTriple
      val asset = Token("zou", issuer.accountId)

      // issuer clears all trustline flags (1+2)
      // to and from both trust
      // then try to send from to to.

      val response = for {
        sequence <- horizon.account.detail(issuer.accountId).map(_.nextSequence)
        response <- horizon.transact(Transaction(
          networkId = horizon.networkId,
          source = issuer.accountId,
          sequence = sequence,
          operations = List(
            TrustAsset(asset, 999, from.address.toOption),
            TrustAsset(asset, 999, to.address.toOption),
            Pay(from.address, Amount(asset, 500), issuer.address.toOption),
            ConfigureTrustLine()
          ),
          maxFee = 999
        ).sign(issuer))
      } yield response
      val response = horizon.transact(from, List(Pay(to.address, Amount(asset, 7))))
      response must beLike[TransactionResponse] { res =>
        res.accepted must beFalse
        res.operationEvents mustEqual List(
          PaymentFailed(
            source = from.address,
            to = to.address,
            amount = Amount(asset, 7L),
            failure = MissingTrustLine
          )
        )
        res.feeCharged mustEqual 100L
        res.validationResult mustEqual Valid
      }.await(0, 30.seconds)
    }

    */

    "fail when the recipient is not authorised to trust this asset" >> pending("support for trustline creation")

    "fail when the amount is zero" >> {
      val (from, to) = testAccountPool.borrowPair
      val response = horizon.transact(from, List(Pay(to.address, Lumen(0))))
      response must beLike[TransactionResponse] { res =>
        res.accepted must beFalse
        res.operationEvents mustEqual List(
          PaymentFailed(
            source = from.address,
            to = to.address,
            amount = Lumen(0),
            failure = InvalidAmount
          )
        )
        res.feeCharged mustEqual 100L
        res.validationResult mustEqual Valid
      }.await(0, 30.seconds)
    }
  }

  "transacting a cross-asset payment" should {
    "allow the sender to specify the exact sending amount" >> pending("requires trade placement")
  }

  // Close the accounts and return their funds back to friendbot
  step { logger.info("Ensuring all tests are complete before closing pool.") }
  step { Await.result(testAccountPool.close(), 10.minute) }

}
