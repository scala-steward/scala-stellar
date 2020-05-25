package stellar.horizon.json

import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, Formats, JsonInput}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.Amount

class AmountReaderSpec extends Specification with ScalaCheck {
  import AmountReaderSpec._
  import stellar.protocol.Amounts._
  implicit val formats: Formats = DefaultFormats + new AmountReader("balance")

  "amounts" should {
    "deserialise from json" >> prop { amount: Amount =>
      parse(asJsonDoc(amount)).extract[Amount] mustEqual amount
    }
  }
}

object AmountReaderSpec {
  def asJsonDoc(amount: Amount): String =
    s"""
       |{
       |  "balance":"${amount.units.toDouble / 10_000_000}"
       |  ${AssetReaderSpec.asJsonDoc(amount.asset).replaceAll("[{}]", "").trim}
       |}""".stripMargin
}