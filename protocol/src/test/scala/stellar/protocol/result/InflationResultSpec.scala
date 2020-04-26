package stellar.protocol.result

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.{AccountIds, XdrSerdeMatchers}

class InflationResultSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import InflationResults._

  "inflation results" should {
    "serialise and deserialise" >> prop { inflationResult: InflationResult =>
      inflationResult must xdrDecodeAndEncode(InflationResult)
    }
  }
}

object InflationResults {
  val genInflationPayout: Gen[InflationPayout] = for {
    recipient <- AccountIds.genAccountId
    units <- Gen.posNum[Long]
  } yield InflationPayout(recipient, units)
  val genInflationNotDue: Gen[InflationResult] = Gen.oneOf(Seq(InflationNotDue))
  val genInflationSuccess: Gen[InflationResult] = Gen.listOf(genInflationPayout).map(InflationSuccess)
  val genInflationResult: Gen[InflationResult] = Gen.frequency(
    1 -> genInflationNotDue,
    39 -> genInflationSuccess
  )
  implicit val arbInflationResult: Arbitrary[InflationResult] = Arbitrary(genInflationResult)
}