package stellar.protocol.result

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.XdrSerdeMatchers

class AccountMergeResultSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import AccountMergeResults._

  "account merge result" should {
    "serialise and deserialise" >> prop { accountMergeResult: AccountMergeResult =>
      accountMergeResult must xdrDecodeAndEncode(AccountMergeResult)
    }
  }
}

object AccountMergeResults {
  val genAccountMergeResult: Gen[AccountMergeResult] = Gen.oneOf(
    Gen.posNum[Long].map(AccountMergeSuccess),
    Gen.const(AccountMergeMalformed),
    Gen.const(AccountMergeNoAccount),
    Gen.const(AccountMergeImmutable),
    Gen.const(AccountMergeHasSubEntries),
    Gen.const(AccountMergeSeqNumTooFar),
    Gen.const(AccountMergeDestinationFull)
  )
  implicit val arbAccountMergeResult: Arbitrary[AccountMergeResult] = Arbitrary(genAccountMergeResult)
}