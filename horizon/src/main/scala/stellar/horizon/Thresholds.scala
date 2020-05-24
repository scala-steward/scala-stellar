package stellar.horizon

import org.json4s.{DefaultFormats, Formats, JObject}
import stellar.horizon.json.JsReader

/**
 * The thresholds for operations on an account.
 *
 * @param low The weight required for a valid transaction including the Allow Trust and Bump Sequence operations.
 * @param med The weight required for a valid transaction including the Create Account, Payment, Path Payment, Manage
 *            Buy Offer, Manage Sell Offer, Create Passive Sell Offer, Change Trust, Inflation, and Manage Data operations.
 * @param high The weight required for a valid transaction including the Account Merge and Set Options operations.
 */
case class Thresholds(low: Int, med: Int, high: Int)

object ThresholdsReader extends JsReader[Thresholds]({ o: JObject =>
  implicit val formats: Formats = DefaultFormats

  Thresholds(
    (o \ "low_threshold").extract[Int],
    (o \ "med_threshold").extract[Int],
    (o \ "high_threshold").extract[Int])
})