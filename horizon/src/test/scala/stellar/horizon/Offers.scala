package stellar.horizon

import org.scalacheck.{Arbitrary, Gen}
import stellar.protocol.Price

object Offers {
  import stellar.protocol.AccountIds._
  import stellar.protocol.Amounts._

  val genOffer: Gen[Offer] = for {
    id <- Gen.posNum[Long]
    seller <- genAccountId
    selling <- genAmount
    buying <- genAmount
    price = Price.from(buying.units, selling.units)
  } yield Offer(id, seller, selling, buying, price)

  implicit val arbOffer: Arbitrary[Offer] = Arbitrary(genOffer)
}
