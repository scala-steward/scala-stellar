package stellar.protocol

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class AssetSpec extends Specification with ScalaCheck {
  import Assets._

  "asset" should {
    "encode and decode" >> prop { asset: Asset =>
      val (remainder, value) = Asset.decode.run(asset.encode).value
      remainder must beEmpty
      value mustEqual asset
    }
  }
}

object Assets {
  import AccountIds._

  val genAsset: Gen[Asset] = Gen.chooseNum(0, 12).flatMap {
    case 0 => Gen.oneOf(Seq(Lumens))
    case n => for {
      code <- Gen.listOfN(n, Gen.alphaNumChar).map(_.mkString)
      issuer <- genAccountId
    } yield Token(code, issuer)
  }
  implicit val arbAsset: Arbitrary[Asset] = Arbitrary(genAsset)
}
