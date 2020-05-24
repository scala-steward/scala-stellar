package stellar.horizon

import org.json4s.{DefaultFormats, Formats, JObject}
import stellar.horizon.json.JsReader

/**
 * The state of authorization of an account.
 *
 * @param required  authorization is required
 * @param revocable authorization can be revoked
 * @param immutable the authorization state can never be changed
 */
case class AuthFlags(required: Boolean, revocable: Boolean, immutable: Boolean)

object AuthFlagsReader extends JsReader[AuthFlags]({ o: JObject =>
  implicit val formats: Formats = DefaultFormats

  AuthFlags(
    (o \ "auth_required").extract[Boolean],
    (o \ "auth_revocable").extract[Boolean],
    (o \ "auth_immutable").extract[Boolean])
})