package stellar.protocol

import net.i2p.crypto.eddsa.{EdDSAPrivateKey, KeyPairGenerator}
import okio.ByteString
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class SeedSpec extends Specification with ScalaCheck {
  import Seeds._

  "seed" should {
    "encode to string" >> prop { seed: Seed =>
      Seed(seed.encodeToString) mustEqual seed
    }
  }
}

object Seeds {
  implicit val arbSeed: Arbitrary[Seed] = Arbitrary(Gen.oneOf(Seq(() =>
    Seed(new ByteString(new KeyPairGenerator().generateKeyPair().getPrivate.asInstanceOf[EdDSAPrivateKey].getAbyte))))
    .map(_.apply()))
}
