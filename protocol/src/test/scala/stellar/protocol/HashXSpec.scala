package stellar.protocol

import okio.ByteString
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class HashXSpec extends Specification with ScalaCheck {
  import HashXs._

  "pre authorised transaction" should {
    "encode and decode" >> prop { hashX: HashX =>
      val (remaining, value) = HashX.decode.run(hashX.encode).value
      remaining must beEmpty
      value mustEqual hashX
    }

    "encode to string" >> prop { hashX: HashX =>
      HashX(hashX.encodeToString) mustEqual hashX
    }
  }
}

object HashXs {
  implicit val arbAccountId: Arbitrary[HashX] = Arbitrary(
    Gen.containerOfN[Array, Byte](32, Arbitrary.arbByte.arbitrary)
      .map(bs => HashX(new ByteString(bs)))
  )
}
