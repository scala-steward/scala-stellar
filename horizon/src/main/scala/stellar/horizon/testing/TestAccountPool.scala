package stellar.horizon.testing

import com.typesafe.scalalogging.LazyLogging
import stellar.horizon.{Balance, Horizon, Offer, TransactionResponse}
import stellar.protocol._
import stellar.protocol.op._

import java.util.concurrent.ConcurrentLinkedQueue
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.IteratorHasAsScala

/**
 * Encapsulates the efficient funding of multiple test accounts and their closing.
 */
class TestAccountPool(
  private val seeds: List[Seed],
  private val friendbotAddress: Address
) extends LazyLogging {

  require(seeds.nonEmpty && seeds.size <= 100,
    s"Can only create between 1 and 100 test accounts (provided $seeds.size)")

  private val free = new ConcurrentLinkedQueue[Seed]()
  private val borrowed = new ConcurrentLinkedQueue[Seed]()
  seeds.foreach(free.add)

  /** Get a unique seed and ensure that it is merged on pool close */
  def borrow: Seed = {
    if (free.isEmpty) throw new IllegalStateException("The pool is empty")
    val s = free.remove()
    borrowed.add(s)
    s
  }

  /** Get a unique seed and let the pool forget about it. It will not be merged on close. */
  def take: Seed = {
    if (free.isEmpty) throw new IllegalStateException("The pool is empty")
    free.remove()
  }

  /** Borrow twice, for a sender and recipient */
  def borrowPair: (Seed, Seed) = (borrow, borrow)

  /** Borrow thrice, for a sender and recipient and once more for good luck */
  def borrowTriple: (Seed, Seed, Seed) = (borrow, borrow, borrow)

  def size: Int = free.size() + borrowed.size()

  def close()(implicit ee: ExecutionContext): Future[List[TransactionResponse]] = {
    if (free.isEmpty && borrowed.isEmpty) Future(Nil)
    else {
      val horizon = Horizon.async(Horizon.Networks.Test)
      val freeCleanUpOperations: Map[Seed, Future[List[Operation]]] = free.iterator().asScala
        .map(seed => seed -> Future(List(
          MergeAccount(destination = friendbotAddress, source = Some(seed.address)))
        )).toMap

      val usedCleanUpOperations: Map[Seed, Future[List[Operation]]] = borrowed.iterator().asScala.map { seed =>
        val operations = for {
          offers <- horizon.account.offers(seed.accountId)
          cancelOfferOps = offers.map(cancelOfferOp)
          balances <- horizon.account.detail(seed.accountId).map(_.balances)
          burnBalances = balances.flatMap(closeBalanceOps)
        } yield cancelOfferOps ++ burnBalances :+
          MergeAccount(destination = friendbotAddress, source = Some(seed.address))
        seed -> operations
      }.toMap

      val allOperations: Future[List[(Seed, Operation)]] = Future.sequence(
        (usedCleanUpOperations ++ freeCleanUpOperations).toList
          .map { case (seed, ops) => ops.map { seed -> _ }}
      ).map { _.flatMap { case (seed, ops) => ops.map { seed -> _ }}}

      val batchedOperations: Future[List[List[(Seed, Operation)]]] =
        allOperations.map { _.foldLeft(List.empty[List[(Seed, Operation)]]) { case (acc, next) =>
          acc match {
            case Nil => List(List(next))
            case h :: _ if h.size == 100 => List(next) :: acc
            case h :: _ if h.distinctBy(_._1).size == 20 && !h.exists(_._1 == next._1) => List(next) :: acc
            case h :: t => next +: h :: t
          }
      }.reverse.map(_.reverse)}

      batchedOperations.flatMap(xss => Future.sequence(xss.map { xs =>
        val seeds = xs.map(_._1).distinct
        val ops = xs.map(_._2)
        horizon.transact(seeds.head, ops, seeds.tail.toSet)
      }))
    }
  }

  private def cancelOfferOp(offer: Offer): CancelBid = CancelBid(offer.id, offer.selling, offer.buying)

  private def closeBalanceOps(balance: Balance): List[Operation] =
    balance.amount.asset.asToken.toList.flatMap(token => List(
      Pay(Address(token.issuer), balance.amount),
      TrustAsset.removeTrust(token)
    ))
}

object TestAccountPool extends LazyLogging {

  def create(quantity: Int)(implicit ec: ExecutionContext): Future[TestAccountPool] = {
    require(quantity >= 1 && quantity <= 100, s"Can only create between 1 and 100 test accounts (provided $quantity)")
    val horizon = Horizon.async(Horizon.Networks.Test)
    val first :: others = LazyList.continually(Seed.random).take(quantity).toList
    val fee = 100 * others.size
    for {
      friendbotCreateResponse <- horizon.friendbot.create(first.accountId)
      _ = logger.info(s"Creating $quantity test accounts.")
      sourceAccountResponse <- horizon.account.detail(first.accountId)
      startingBalance = sourceAccountResponse.balance(Lumen).map(_.units - fee).map(_ / quantity).get
      _ <- if (others.nonEmpty) horizon.transact(Transaction(
        networkId = horizon.networkId,
        source = first.accountId,
        sequence = sourceAccountResponse.nextSequence,
        operations = others.map(seed => CreateAccount(seed.accountId, startingBalance)),
        maxFee = fee
      ).sign(first)
      ) else Future.unit
      pool = new TestAccountPool(first :: others, friendbotCreateResponse.operationEvents.head.source)
    } yield pool
  }
}