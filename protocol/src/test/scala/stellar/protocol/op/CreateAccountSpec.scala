package stellar.protocol.op

import org.specs2.mutable.Specification
import stellar.protocol.{AccountId, Address, Lumen}

class CreateAccountSpec extends Specification {

  "account creation" should {
    "encode correctly" >> {
      CreateAccount(AccountId("GBUW2YWFIUR45JOA7DKN746EUB6G53VSRFCLNP442Z66I4ERSFPJ6KQI"), Lumen(10).units)
        .xdrEncode.encode().base64() must
        beEqualTo("AAAAAAAAAAAAAAAAaW1ixUUjzqXA+NTf88SgfG7usolEtr+c1n3kcJGRXp8AAAAABfXhAA==")
    }

    "encode with an explicit source account" >> {
      CreateAccount(
        accountId = AccountId("GBUW2YWFIUR45JOA7DKN746EUB6G53VSRFCLNP442Z66I4ERSFPJ6KQI"),
        startingBalance = Lumen(10).units,
        source = Some(Address(
          address = "GCKIBOAMMHGDZI5UNCUUWXSUBENHZ65AYM4MYFEOGRMLB4IZXC63WZDR",
          memoId = 88_888_888L
        ))
      ).xdrEncode.encode().base64() must
        beEqualTo("AAAAAQAAAQAAAAAABUxWOJSAuAxhzDyjtGipS15UCRp8+6DDOMwUjjRYsPEZuL27AAAAAAAAAABpbWLFRSPOpcD41N" +
          "/zxKB8bu6yiUS2v5zWfeRwkZFenwAAAAAF9eEA")
    }
  }

}
