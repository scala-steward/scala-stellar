package stellar.horizon.json

import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, Formats}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.horizon.Offer
import stellar.protocol.{Asset, Lumen}

class OfferReaderSpec extends Specification with ScalaCheck {
  import OfferReaderSpec._
  import stellar.horizon.Offers._
  implicit val formats: Formats = DefaultFormats + OfferReader

  "offers" should {
    "deserialise from json" >> prop { offer: Offer =>
      parse(asJsonDoc(offer)).extract[Offer] mustEqual offer
    }
  }
}

object OfferReaderSpec {
  def asJsonDoc(offer: Offer): String = {
    s"""{
       |  "id": "${offer.id}",
       |  "paging_token": "${offer.id}",
       |  "seller": "${offer.seller.encodeToString}",
       |  "selling": ${AssetReaderSpec.asJsonDoc(offer.selling.asset)},
       |  "buying": ${AssetReaderSpec.asJsonDoc(offer.buying.asset)},
       |  "amount": "${offer.selling.units.toDouble / Asset.BASE_UNITS_PER_WHOLE_UNIT}",
       |  "price_r": {
       |    "n": ${offer.price.n},
       |    "d": ${offer.price.d}
       |  },
       |  "price": "${offer.price.n.toDouble / offer.price.d}",
       |}""".stripMargin
  }
}