package stellar.protocol.result

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.XdrSerdeMatchers

class ManageDataResultSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import ManageDataResults._

  "create account result" should {
    "serialise and deserialise" >> prop { result: ManageDataResult =>
      result must xdrDecodeAndEncode(ManageDataResult)
    }
  }
}

object ManageDataResults {
  def genManageDataResult: Gen[ManageDataResult] = Gen.oneOf(
    ManageDataSuccess, ManageDataNotSupportedYet, DeleteDataNameNotFound, AddDataLowReserve, AddDataInvalidName
  )
  implicit val arbManageDataResult: Arbitrary[ManageDataResult] = Arbitrary(genManageDataResult)
}