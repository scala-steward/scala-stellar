package stellar.protocol.op

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol._

class PaymentSpec extends Specification with ScalaCheck with XdrSerdeMatchers {

  "a payment" should {
    "encode when sending lumens to a standard address" >> {
      Pay(
        recipient = Address(AccountId("GCXMCVO2VP33Z7T5TYAQJMV6FSSFTPL5VG356UEELYIPC5ZHRBPK67JX")),
        amount = Lumen(420)
      ).xdrEncode.encode().base64() must
        beEqualTo("AAAAAAAAAAEAAAAArsFV2qv3vP59ngEEsr4spFm9fam331CEXhDxdyeIXq8AAAAAAAAAAPpW6gA=")
    }

    "encode when sending asset <= 4 to a multiplexed address" >> {
      Pay(
        recipient = Address(
          address = "GCKIBOAMMHGDZI5UNCUUWXSUBENHZ65AYM4MYFEOGRMLB4IZXC63WZDR",
          memoId = 88_888_888L
        ),
        amount = Amount(
          asset = Token("GOLD", AccountId("GCXMCVO2VP33Z7T5TYAQJMV6FSSFTPL5VG356UEELYIPC5ZHRBPK67JX")),
          units = 10_000_420L
        )
      ).xdrEncode.encode().base64() must
        beEqualTo("AAAAAAAAAAEAAAEAAAAAAAVMVjiUgLgMYcw8o7RoqUteVAkafPugwzjMFI40WLDxGbi9uwAAAAFHT0xEAAAAAK7BVdqr97z" +
          "+fZ4BBLK+LKRZvX2pt99QhF4Q8XcniF6vAAAAAACYmCQ=")
    }

    "encode when sending asset length 5..12 with an explicit source account" >> {
      Pay(
        recipient = Address("GCXMCVO2VP33Z7T5TYAQJMV6FSSFTPL5VG356UEELYIPC5ZHRBPK67JX"),
        amount = Amount(Token("BTC", AccountId("GCXMCVO2VP33Z7T5TYAQJMV6FSSFTPL5VG356UEELYIPC5ZHRBPK67JX")), 554L),
        source = Some(Address(
          address = "GCKIBOAMMHGDZI5UNCUUWXSUBENHZ65AYM4MYFEOGRMLB4IZXC63WZDR",
          memoId = 88_888_888L
        ))
      ).xdrEncode.encode().base64() must
        beEqualTo("AAAAAQAAAQAAAAAABUxWOJSAuAxhzDyjtGipS15UCRp8+6DDOMwUjjRYsPEZuL27AAAAAQAAAACuwVXaq/e8/" +
          "n2eAQSyviykWb19qbffUIReEPF3J4herwAAAAFCVEMAAAAAAK7BVdqr97z+fZ4BBLK+LKRZvX2pt99QhF4Q8XcniF6vAAAAAAAAAio=")
    }
  }
}

object Payments {
/*
  val genPayment: Gen[Payment] = for {
    sender <- Gen.option(genAddress)
    recipient <- genAddress
    amount <- genAmount
  } yield Payment(sender, recipient, amount)
  implicit val arbPayment: Arbitrary[Payment] = Arbitrary(genPayment)
*/
}