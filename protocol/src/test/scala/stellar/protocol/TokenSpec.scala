package stellar.protocol

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import scala.util.Try

class TokenSpec extends Specification with ScalaCheck {

  "token" should {
    "disallow non-alphanumeric characters" >> {
      Try(Token("こうぎら", Seed.random.accountId)) should beFailedTry[Token]
    }

    "succeed for 1 character" >> {
      Try(Token("Z", Seed.random.accountId)) should beASuccessfulTry[Token]
    }

    "succeed for <= 4 characters" >> {
      Try(Token("BTC", Seed.random.accountId)) should beASuccessfulTry[Token]
    }

    "succeed for > 4 characters" >> {
      Try(Token("BITCONR", Seed.random.accountId)) should beASuccessfulTry[Token]
    }

    "succeed for 12 characters" >> {
      Try(Token("SatoNakamoto", Seed.random.accountId)) should beASuccessfulTry[Token]
    }

    "fail for more than 12 characters" >> {
      Try(Token("SatoshiNakamoto", Seed.random.accountId)) should beFailedTry[Token]
    }

    "fail for empty string" >> {
      Try(Token("", Seed.random.accountId)) should beFailedTry[Token]
    }
  }
}

object Assets {
  import AccountIds._

  val genAsset: Gen[Asset] = Gen.chooseNum(0, 12).flatMap {
    case 0 => Gen.const(Lumen)
    case n => genToken(n)
  }

  val genToken: Gen[Token] = Gen.choose(1, 12).flatMap(genToken(_))

  private def genToken(length: Int) = for {
    code <- Gen.listOfN(length, Gen.alphaNumChar).map(_.mkString)
    issuer <- genAccountId
  } yield Token(code, issuer)

  implicit val arbAsset: Arbitrary[Asset] = Arbitrary(genAsset)
}
