package stellar.protocol.result

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.XdrSerdeMatchers

class BumpSequenceResultSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import BumpSequenceResults._

  "bump sequence result" should {
    "serialise and deserialise" >> prop { result: BumpSequenceResult =>
      result must xdrDecodeAndEncode(BumpSequenceResult)
    }
  }
}

object BumpSequenceResults {
  val genBumpSequenceResult: Gen[BumpSequenceResult] =
    Gen.oneOf(BumpSequenceSuccess, BumpSequenceBadSeqNo)
  implicit val arbBumpSequenceResult: Arbitrary[BumpSequenceResult] = Arbitrary(genBumpSequenceResult)
}
