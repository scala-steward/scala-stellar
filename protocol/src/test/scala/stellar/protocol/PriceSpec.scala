package stellar.protocol

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class PriceSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import Prices._

  "price" should {
    "serialise and deserialise" >> prop { price: Price =>
      // price must xdrDecodeAndEncode(Price)
      pending
    }
  }
}

object Prices {
  val genPrice: Gen[Price] = for {
    n <- Gen.posNum[Int]
    d <- Gen.posNum[Int]
  } yield Price(n, d)
  implicit val arbPrice: Arbitrary[Price] = Arbitrary(genPrice)
}
