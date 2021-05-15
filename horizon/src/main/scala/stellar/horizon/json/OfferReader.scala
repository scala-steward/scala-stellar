package stellar.horizon.json

import org.json4s.JsonAST.JObject
import org.json4s.{DefaultFormats, Formats}
import stellar.horizon._
import stellar.horizon.json.JsReader.doubleStringToLong
import stellar.protocol.{AccountId, Amount, Asset, Price}

object OfferReader extends JsReader[Offer]({ o: JObject =>
  implicit val formats: Formats = DefaultFormats + AssetReader

  val id = (o \ "id").extract[String].toLong
  val seller = AccountId((o \ "seller").extract[String])
  val buyingAsset = (o \ "buying").extract[Asset]
  val sellingAsset = (o \ "selling").extract[Asset]
  val sellingUnits = doubleStringToLong("amount", o)
  val selling = Amount(sellingAsset, sellingUnits)
  val price = Price((o \ "price_r" \ "n").extract[Int], (o \ "price_r" \ "d").extract[Int])
  val buying = Amount(buyingAsset, price.times(sellingUnits))
  Offer(id, seller, selling, buying, price)
})

