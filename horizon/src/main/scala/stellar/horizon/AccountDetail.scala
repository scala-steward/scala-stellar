package stellar.horizon

import java.time.ZonedDateTime

import org.json4s.JsonAST.JObject
import org.json4s.{DefaultFormats, Formats}
import stellar.horizon.json.JsReader
import stellar.protocol.AccountId

case class AccountDetail(id: AccountId,
                         sequence: Long,
                         lastModifiedLedger: Long,
                         lastModifiedTime: ZonedDateTime,
                         subEntryCount: Int,
                         thresholds: Thresholds,
                         authRequired: Boolean,
                         authRevocable: Boolean,
                         //                         balances: List[Balance],
                         //                         signers: List[Signer],
                         //                         data: Map[String, Array[Byte]]
                        )

object AccountDetail {
}

object AccountDetailReader extends JsReader[AccountDetail]({ o: JObject =>
  implicit val formats: Formats = DefaultFormats + ThresholdsReader

  val id = AccountId((o \ "id").extract[String])
  val seq = (o \ "sequence").extract[String].toLong
  val lastModifiedLedger = (o \ "last_modified_ledger").extract[Long]
  val lastModifiedTime = ZonedDateTime.parse((o \ "last_modified_time").extract[String])
  val subEntryCount = (o \ "subentry_count").extract[Int]
  val thresholds = (o \ "thresholds").extract[Thresholds]
  val authRequired = (o \ "flags" \ "auth_required").extract[Boolean]
  val authRevocable = (o \ "flags" \ "auth_revocable").extract[Boolean]
/*
  val JArray(jsBalances) = o \ "balances"
  val balances = jsBalances.map {
    case balObj: JObject =>
      val units = toBaseUnits((balObj \ "balance").extract[String].toDouble).get
      val amount = (balObj \ "asset_type").extract[String] match {
        case "credit_alphanum4" =>
          Amount(units, IssuedAsset4(
            code = (balObj \ "asset_code").extract[String],
            issuer = KeyPair.fromAccountId((balObj \ "asset_issuer").extract[String])
          ))
        case "credit_alphanum12" =>
          Amount(units, IssuedAsset12(
            code = (balObj \ "asset_code").extract[String],
            issuer = KeyPair.fromAccountId((balObj \ "asset_issuer").extract[String])
          ))
        case "native" => NativeAmount(units)
        case t => throw new RuntimeException(s"Unrecognised asset type: $t")
      }
      val limit = (balObj \ "limit").extractOpt[String].map(BigDecimal(_)).map(toBaseUnits).map(_.get)
      val buyingLiabilities = toBaseUnits(BigDecimal((balObj \ "buying_liabilities").extract[String])).get
      val sellingLiabilities = toBaseUnits(BigDecimal((balObj \ "selling_liabilities").extract[String])).get
      val authorised = (balObj \ "is_authorized").extractOpt[Boolean].getOrElse(false)
      val authorisedToMaintainLiabilities = (balObj \ "is_authorized_to_maintain_liabilities")
        .extractOpt[Boolean].getOrElse(false)

      Balance(amount, limit, buyingLiabilities, sellingLiabilities, authorised, authorisedToMaintainLiabilities)
    case _ => throw new RuntimeException(s"Expected js object at 'balances'")
  }
  val JArray(jsSigners) = o \ "signers"
  val signers = jsSigners.map {
    case signerObj: JObject =>
      val key = StrKey.decodeFromString((signerObj \ "key").extract[String]).asInstanceOf[SignerStrKey]
      val weight = (signerObj \ "weight").extract[Int]
      Signer(key, weight)
    case _ => throw new RuntimeException(s"Expected js object at 'signers'")
  }
*/
//  val JObject(dataFields) = o \ "data"
//  val data = dataFields.map{ case (k, v) => k -> ByteArrays.base64(v.extract[String]) }.toMap

  AccountDetail(id, seq, lastModifiedLedger, lastModifiedTime, subEntryCount, thresholds,
    authRequired, authRevocable
    /*, balances, signers, data*/)

})
