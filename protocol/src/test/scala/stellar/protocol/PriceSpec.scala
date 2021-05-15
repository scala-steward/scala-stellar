package stellar.protocol

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class PriceSpec extends Specification with ScalaCheck with XdrSerdeMatchers {

  "price" should {
    "always be formed in the correct ratio" >> prop { (n: Long, d: Long) =>
      val lcdPrice = Price.from(n, d)
      lcdPrice.n.toDouble  / n.toDouble mustEqual lcdPrice.d.toDouble / d.toDouble
    }.setGens(Gen.posNum[Long], Gen.posNum[Long])

    "be formed with a lower but still correct ratio where possible" >> {
      Price.from(450, 500) mustEqual Price(9, 10)
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
