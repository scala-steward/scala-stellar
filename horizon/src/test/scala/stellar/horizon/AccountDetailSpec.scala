package stellar.horizon

import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME
import java.time.{Instant, ZoneId}

import org.json4s.native.JsonMethods.parse
import org.json4s.native.Serialization
import org.json4s.{Formats, NoTypeHints}
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class AccountDetailSpec extends Specification with ScalaCheck {
  import AccountDetails._

  "account detail" should {
    "deserialise from json" >> prop { accountDetail: AccountDetail =>
      parse(asJsonDoc(accountDetail)).extract[AccountDetail] mustEqual accountDetail
    }
  }

}

object AccountDetails {
  import stellar.protocol.AccountIds._

  val genAccountDetail: Gen[AccountDetail] = for {
    accountId <- genAccountId
    sequence <- Gen.posNum[Long]
    lastModifiedLedger <- Gen.posNum[Long]
    lastModifiedTime <- Gen.posNum[Long].map(Instant.ofEpochMilli)
      .map(_.atZone(ZoneId.of("Z")).withNano(0))
    subEntryCount <- Gen.posNum[Int]
    thresholds <- Gen.listOfN(3, Gen.posNum[Int]).map{ case List(l, m, h) => Thresholds(l, m, h) }
    authRequired <- Gen.oneOf(true, false)
    authRevocable <- Gen.oneOf(true, false)
  } yield AccountDetail(accountId, sequence, lastModifiedLedger, lastModifiedTime, subEntryCount,
    thresholds, authRequired, authRevocable)

  implicit val arbAccountDetail: Arbitrary[AccountDetail] = Arbitrary(genAccountDetail)

  implicit val formats: Formats = Serialization.formats(NoTypeHints) + AccountDetailReader

  def asJsonDoc(detail: AccountDetail): String = {
    val id = detail.id.encodeToString
    s"""
      |{
      |  "_links": {
      |    "self": {
      |      "href": "https://horizon-testnet.stellar.org/accounts/$id"
      |    },
      |    "transactions": {
      |      "href": "https://horizon-testnet.stellar.org/accounts/$id/transactions{?cursor,limit,order}",
      |      "templated": true
      |    },
      |    "operations": {
      |      "href": "https://horizon-testnet.stellar.org/accounts/$id/operations{?cursor,limit,order}",
      |      "templated": true
      |    },
      |    "payments": {
      |      "href": "https://horizon-testnet.stellar.org/accounts/$id/payments{?cursor,limit,order}",
      |      "templated": true
      |    },
      |    "effects": {
      |      "href": "https://horizon-testnet.stellar.org/accounts/$id/effects{?cursor,limit,order}",
      |      "templated": true
      |    },
      |    "offers": {
      |      "href": "https://horizon-testnet.stellar.org/accounts/$id/offers{?cursor,limit,order}",
      |      "templated": true
      |    },
      |    "trades": {
      |      "href": "https://horizon-testnet.stellar.org/accounts/$id/trades{?cursor,limit,order}",
      |      "templated": true
      |    },
      |    "data": {
      |      "href": "https://horizon-testnet.stellar.org/accounts/$id/data/{key}",
      |      "templated": true
      |    }
      |  },
      |  "id": "$id",
      |  "account_id": "$id",
      |  "sequence": "${detail.sequence}",
      |  "subentry_count": ${detail.subEntryCount},
      |  "last_modified_ledger": ${detail.lastModifiedLedger},
      |  "last_modified_time": "${detail.lastModifiedTime.format(ISO_ZONED_DATE_TIME)}",
      |  "thresholds": {
      |    "low_threshold": ${detail.thresholds.low},
      |    "med_threshold": ${detail.thresholds.med},
      |    "high_threshold": ${detail.thresholds.high}
      |  },
      |  "flags": {
      |    "auth_required": ${detail.authRequired},
      |    "auth_revocable": ${detail.authRevocable},
      |    "auth_immutable": false
      |  },
      |  "balances": [
      |    {
      |      "balance": "10000.0000000",
      |      "buying_liabilities": "0.0000000",
      |      "selling_liabilities": "0.0000000",
      |      "asset_type": "native"
      |    }
      |  ],
      |  "signers": [
      |    {
      |      "weight": 1,
      |      "key": "$id",
      |      "type": "ed25519_public_key"
      |    }
      |  ],
      |  "data": {},
      |  "paging_token": "$id"
      |}""".stripMargin
  }

}