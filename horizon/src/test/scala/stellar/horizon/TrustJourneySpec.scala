package stellar.horizon

import com.typesafe.scalalogging.LazyLogging
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import stellar.event.TrustChangeFailed.{CannotRemoveTrustLine, InsufficientTrustLineLimit, IssuerDoesNotExist}
import stellar.event.{TrustChangeFailed, TrustChanged, TrustRemoved}
import stellar.horizon.ValidationResult.Valid
import stellar.horizon.testing.TestAccountPool
import stellar.protocol.op.{Pay, TrustAsset}
import stellar.protocol.{Amount, Seed, Token, Transaction}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class TrustJourneySpec(implicit ee: ExecutionEnv) extends Specification with LazyLogging {

  private val horizon = Horizon.sync(Horizon.Networks.Test)
  private lazy val testAccountPool = Await.result(TestAccountPool.create(20), 1.minute)

  "trusting an asset" should {

    "fail when the asset issuer does not exist" >> {
      val trustor = testAccountPool.borrow
      val trustee = Seed.random
      val asset = Token("BTC", trustee.accountId)
      val response = for {
        fromAccountDetails <- horizon.account.detail(trustor.accountId)
        response <- horizon.transact(Transaction(
          networkId = horizon.networkId,
          source = trustor.accountId,
          sequence = fromAccountDetails.nextSequence,
          operations = List(
            TrustAsset(asset, 100_000_000L)
          ),
          maxFee = 100
        ).sign(trustor))
      } yield response

      response must beASuccessfulTry[TransactionResponse].like { res =>
        res.accepted must beFalse
        res.operationEvents mustEqual List(
          TrustChangeFailed(
            source = trustor.address,
            failure = IssuerDoesNotExist
          )
        )
        res.feeCharged mustEqual 100L
        res.validationResult mustEqual Valid
      }
    }

    "fail when the lumen balance minus the lumen selling liabilities is insufficient to cover a new trustline entry" >>
      pending("trading")

    "fail when attempting to trust yourself" >> {
      val account = testAccountPool.borrow
      val asset = Token("BTC", account.accountId)
      val response = for {
        fromAccountDetails <- horizon.account.detail(account.accountId)
        response <- horizon.transact(Transaction(
          networkId = horizon.networkId,
          source = account.accountId,
          sequence = fromAccountDetails.nextSequence,
          operations = List(
            TrustAsset(asset, 100_000_000L)
          ),
          maxFee = 100
        ).sign(account))
      } yield response

      // response.failed.foreach(_.printStackTrace())
      response must beAFailedTry[TransactionResponse]

      // TODO - this isn't working. Should be CannotTrustSelf, but horizon is returning an malformed error.
      /*
            response must beASuccessfulTry[TransactionResponse].like { res =>
              res.accepted must beFalse
              res.operationEvents mustEqual List(
                TrustChangeFailed(
                  source = account.address,
                  failure = CannotTrustSelf // TODO
                )
              )
              res.feeCharged mustEqual 100L
              res.validationResult mustEqual Valid
            }
      */
    }

    "otherwise succeed" >> {
      val (trustee, trustor) = testAccountPool.borrowPair
      val asset = Token("BTC", trustee.accountId)
      val response = for {
        fromAccountDetails <- horizon.account.detail(trustor.accountId)
        response <- horizon.transact(Transaction(
          networkId = horizon.networkId,
          source = trustor.accountId,
          sequence = fromAccountDetails.nextSequence,
          operations = List(
            TrustAsset(asset, 100_000_000L)
          ),
          maxFee = 100
        ).sign(trustor))
      } yield response

      response must beASuccessfulTry[TransactionResponse].like { res =>
        res.accepted must beTrue
        res.operationEvents mustEqual List(
          TrustChanged(
            source = trustor.address,
            token = asset,
            limit = 100_000_000L
          )
        )
        res.feeCharged mustEqual 100L
        res.validationResult mustEqual Valid
      }
    }
  }

  "changing the trust level for an asset" should {
    "fail when the new limit is insufficient to cover the existing balance" >> {
      val (trustee, trustor) = testAccountPool.borrowPair
      val asset = Token("KOUGIRA", trustee.accountId)
      val response = for {
        _ <- horizon.transact(trustor, List(
          TrustAsset(asset, 100_000_000L),
          Pay(trustor.address, Amount(asset, 5_000_000L), Some(trustee.address))
        ), Set(trustee))
        r <- horizon.transact(trustor, List(TrustAsset(asset, 2_000_000L)))
      } yield r
      response must beSuccessfulTry[TransactionResponse].like { res =>
        res.accepted must beFalse
        res.operationEvents mustEqual List(TrustChangeFailed(trustor.address, InsufficientTrustLineLimit))
        res.feeCharged mustEqual 100L
        res.validationResult mustEqual Valid
      }
    }

    "fail when the new limit is sufficient to cover balance, but not the balance plus buying liabilities" >> pending("trading")

    "succeed otherwise" >> {
      val (trustee, trustor) = testAccountPool.borrowPair
      val asset = Token("KOUGIRA", trustee.accountId)
      val response = for {
        _ <- horizon.transact(trustor, List(
          TrustAsset(asset, 100_000_000L),
          Pay(trustor.address, Amount(asset, 5_000_000L), Some(trustee.address))
        ), Set(trustee))
        r <- horizon.transact(trustor, List(TrustAsset(asset, 7_000_000L)))
      } yield r
      response must beSuccessfulTry[TransactionResponse].like { res =>
        res.accepted must beTrue
        res.operationEvents mustEqual List(TrustChanged(trustor.address, asset, 7_000_000L))
        res.feeCharged mustEqual 100L
        res.validationResult mustEqual Valid
      }
    }
  }

  "removing trust of an asset" should {
    "succeed when trust exists" >> {
      val (trustee, trustor) = testAccountPool.borrowPair
      val asset = Token("KOUGIRA", trustee.accountId)
      val response = horizon.transact(trustor, List(TrustAsset(asset, 100_000_000L)))
        .flatMap(_ => horizon.transact(trustor, List(TrustAsset.removeTrust(asset))))
      response must beSuccessfulTry[TransactionResponse].like { res =>
        res.accepted must beTrue
        res.operationEvents mustEqual List(
          TrustRemoved(
            source = trustor.address,
            token = asset
          )
        )
        res.feeCharged mustEqual 100L
        res.validationResult mustEqual Valid
      }
    }

    "fail when trust does not exist" >> {
      val (trustee, trustor) = testAccountPool.borrowPair
      val asset = Token("KOUGIRA", trustee.accountId)
      val response = horizon.transact(trustor, List(TrustAsset.removeTrust(asset)))
      response must beSuccessfulTry[TransactionResponse].like { res =>
        res.accepted must beFalse
        res.operationEvents mustEqual List(TrustChangeFailed(trustor.address, CannotRemoveTrustLine))
        res.feeCharged mustEqual 100L
        res.validationResult mustEqual Valid
      }
    }

    "fail when a balance remains" >> {
      val (trustee, trustor) = testAccountPool.borrowPair
      val asset = Token("KOUGIRA", trustee.accountId)
      val response = for {
        _ <- horizon.transact(trustor, List(
            TrustAsset(asset, 100_000_000L),
            Pay(trustor.address, Amount(asset, 5_000_000L), Some(trustee.address))
          ), Set(trustee))
        r <- horizon.transact(trustor, List(TrustAsset.removeTrust(asset)))
      } yield r
      response must beSuccessfulTry[TransactionResponse].like { res =>
        res.accepted must beFalse
        res.operationEvents mustEqual List(TrustChangeFailed(trustor.address, CannotRemoveTrustLine))
        res.feeCharged mustEqual 100L
        res.validationResult mustEqual Valid
      }
    }
  }

  // Close the accounts and return their funds back to friendbot
  step { logger.info("Ensuring all tests are complete before closing pool.") }
  step { Await.result(testAccountPool.close(), 10.minute) }

}
