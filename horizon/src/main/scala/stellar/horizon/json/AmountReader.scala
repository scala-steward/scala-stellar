package stellar.horizon.json

import org.json4s.{DefaultFormats, Formats}
import org.json4s.JsonAST.JObject
import stellar.protocol.{Amount, Asset}

class AmountReader(unitsTag: String) extends JsReader[Amount]({ o: JObject =>
  implicit val format: Formats = DefaultFormats + AssetReader

  Amount(
    units = (BigDecimal((o \ unitsTag).extract[String]) * BigDecimal(10).pow(7)).toLongExact,
    asset = (o).extract[Asset]
  )
})
