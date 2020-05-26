package stellar.horizon.json

import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, Formats}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.{AccountId, HashX, PreAuthTx, Signer}

class SignerReaderSpec extends Specification with ScalaCheck {
  import SignerReaderSpec._
  import stellar.protocol.Signers._
  implicit val formats: Formats = DefaultFormats + SignerReader

  "signers" should {
    "deserialise from json" >> prop { signer: Signer =>
      parse(asJsonDoc(signer)).extract[Signer] mustEqual signer
    }
  }
}

object SignerReaderSpec {
  def asJsonDoc(signer: Signer): String = {
    val keyType = signer.key match {
      case AccountId(_) => "ed25519_public_key"
      case HashX(_) => "sha256_hash"
      case PreAuthTx(_) => "preauth_tx"
    }
    s"""{
       |  "key":"${signer.key.encodeToString}"
       |  "type":"$keyType"
       |  "weight":${signer.weight}
       |}""".stripMargin
  }
}