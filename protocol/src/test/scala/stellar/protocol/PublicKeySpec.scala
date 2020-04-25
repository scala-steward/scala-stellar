package stellar.protocol

import net.i2p.crypto.eddsa.{EdDSAPublicKey, KeyPairGenerator}
import okio.ByteString
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class PublicKeySpec extends Specification with ScalaCheck {

  "public key should encode and decode" >> prop { pk: PublicKey =>
    val (remaining, value) = PublicKey.decode.run(pk.encode).value
    remaining must beEmpty
    value mustEqual pk
  }

  implicit val arbPublicKey: Arbitrary[PublicKey] = Arbitrary(Gen.oneOf(Seq(
    PublicKey(new ByteString(new KeyPairGenerator().generateKeyPair().getPublic.asInstanceOf[EdDSAPublicKey].getAbyte)))))
}
