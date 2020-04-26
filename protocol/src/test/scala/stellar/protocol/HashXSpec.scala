package stellar.protocol

import okio.ByteString
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class HashXSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import HashXs._

  "pre authorised transaction" should {
    "encode and decode" >> prop { hashX: HashX =>
      hashX must xdrDecodeAndEncode(HashX)
    }

    "encode to string" >> prop { hashX: HashX =>
      HashX(hashX.encodeToString) mustEqual hashX
    }
  }
}

object HashXs {
  val genHashX: Gen[HashX] = Gen.containerOfN[Array, Byte](32, Arbitrary.arbByte.arbitrary)
    .map(bs => HashX(new ByteString(bs)))
  implicit val arbAccountId: Arbitrary[HashX] = Arbitrary(genHashX)
}
