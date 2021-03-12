package stellar.protocol.op

import org.specs2.mutable.Specification
import stellar.protocol._

class TrustAssetSpec extends Specification {

  private val issuer = AccountId("GBDYFFQ7GFQ76JZXJRPPFKT2F4S25NZDQPBEKFKB3Y3BZ5EQA52REB2M")

  "trusting an asset" should {
    "encode when setting maximum limit" >> {
      TrustAsset(
        asset = Token("Ford", issuer),
        limit = Long.MaxValue
      ).xdrEncode.encode().base64() must
        beEqualTo("AAAAAAAAAAYAAAABRm9yZAAAAABHgpYfMWH/JzdMXvKqei8lrrcjg8JFFUHeNhz0kAd1En//////////")
    }

    "encode when removing the trust altogether" >> {
      TrustAsset.removeTrust(
        asset = Token("Trinity", issuer),
        source = Some(Address("GD5BFPBWPZINGSQX7KMY2QQ733JBVT325GSXA5MCSONDWIM3FXIRGFFD", 42L))
      ).xdrEncode.encode().base64() must
        beEqualTo("AAAAAQAAAQAAAAAAAAAAKvoSvDZ+UNNKF/qZjUIf3tIaz3rppXB1gpOaOyGbLdETAAAABgAAAAJUcmluaXR5AAAAAAAAAAAAR" +
          "4KWHzFh/yc3TF7yqnovJa63I4PCRRVB3jYc9JAHdRIAAAAAAAAAAA==")
    }

    "encode when an explicit source account is provided" >> {
      TrustAsset(
        asset = Token("Zaphod", issuer),
        limit = 420_000_000L,
        source = Some(Address("GD5BFPBWPZINGSQX7KMY2QQ733JBVT325GSXA5MCSONDWIM3FXIRGFFD", 42L))
      ).xdrEncode.encode().base64() must
        beEqualTo("AAAAAQAAAQAAAAAAAAAAKvoSvDZ+UNNKF/qZjUIf3tIaz3rppXB1gpOaOyGbLdETAAAABgAAAAJaYXBob2QAAAAAAAAAAAAAR" +
          "4KWHzFh/yc3TF7yqnovJa63I4PCRRVB3jYc9JAHdRIAAAAAGQixAA==")
    }
  }

}
