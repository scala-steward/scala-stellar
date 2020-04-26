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
  private val genKeyPair = Gen.const(() => new KeyPairGenerator().generateKeyPair()).map(_.apply())
  private val genPrivateKey = genKeyPair.map(_.getPrivate.asInstanceOf[EdDSAPrivateKey])

  val genSeed: Gen[Seed] = genPrivateKey.map(_.getAbyte).map(new ByteString(_)).map(Seed(_))
  implicit val arbSeed: Arbitrary[Seed] = Arbitrary(genSeed)
}
