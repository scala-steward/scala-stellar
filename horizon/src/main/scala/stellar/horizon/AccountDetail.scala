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
                         authFlags: AuthFlags,
                         balances: List[Balance],
                         //                         signers: List[Signer],
                         //                         data: Map[String, Array[Byte]]
                        )

object AccountDetailReader extends JsReader[AccountDetail]({ o: JObject =>
  implicit val formats: Formats = DefaultFormats + ThresholdsReader + AuthFlagsReader + BalanceReader

  val id = AccountId((o \ "id").extract[String])
  val seq = (o \ "sequence").extract[String].toLong
  val lastModifiedLedger = (o \ "last_modified_ledger").extract[Long]
  val lastModifiedTime = ZonedDateTime.parse((o \ "last_modified_time").extract[String])
  val subEntryCount = (o \ "subentry_count").extract[Int]
  val thresholds = (o \ "thresholds").extract[Thresholds]
  val flags = (o \ "flags").extract[AuthFlags]
  val balances = (o \ "balances").extract[List[Balance]]

  AccountDetail(id, seq, lastModifiedLedger, lastModifiedTime, subEntryCount, thresholds, flags, balances

    /*, signers, data*/)

})
