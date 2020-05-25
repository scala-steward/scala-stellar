package stellar.horizon.json

import org.json4s.{DefaultFormats, Formats}
import org.json4s.JsonAST.JObject
import stellar.protocol.{Amount, Asset}

class AmountReader(unitsTag: String) extends JsReader[Amount]({ o: JObject =>
  import JsReader.doubleStringToLong
  implicit val format: Formats = DefaultFormats + AssetReader

  Amount(
    units = doubleStringToLong(unitsTag, o),
    asset = (o).extract[Asset]
  )
})
