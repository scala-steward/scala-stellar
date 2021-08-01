package stellar.protocol

import java.nio.charset.StandardCharsets
import okio.ByteString
import org.stellar.xdr
import org.stellar.xdr.Asset.{AssetAlphaNum12, AssetAlphaNum4}
import org.stellar.xdr.{AssetCode12, AssetCode4, AssetType}
import stellar.protocol.Asset.BASE_UNITS_PER_WHOLE_UNIT

sealed trait Asset {
  val code: String
  val asToken: Option[Token] = None
  def xdrEncode: xdr.Asset
}

object Asset {
  val BASE_UNITS_PER_WHOLE_UNIT = 10_000_000L
  def decode(x: xdr.Asset): Asset = {
    x.getDiscriminant match {
      case AssetType.ASSET_TYPE_NATIVE => Lumen
      case AssetType.ASSET_TYPE_CREDIT_ALPHANUM4 =>
        Token(
          code = new String(x.getAlphaNum4.getAssetCode.getAssetCode4, StandardCharsets.UTF_8).trim(),
          issuer = AccountId.decode(x.getAlphaNum4.getIssuer)
        )
      case AssetType.ASSET_TYPE_CREDIT_ALPHANUM12 =>
        Token(
          code = new String(x.getAlphaNum12.getAssetCode.getAssetCode12, StandardCharsets.UTF_8).trim(),
          issuer = AccountId.decode(x.getAlphaNum12.getIssuer)
        )
    }
  }
}

/**
 * The network's native asset, XLM.
 */
case object Lumen extends Asset {
  val STROOPS_PER_LUMEN: Long = BASE_UNITS_PER_WHOLE_UNIT

  override val code: String = "XLM"
  def apply(lumen: Int): Amount = stroops(lumen * STROOPS_PER_LUMEN)
  def stroops(stroops: Long): Amount = Amount(Lumen, stroops)

  override def xdrEncode: xdr.Asset = new xdr.Asset.Builder()
    .discriminant(AssetType.ASSET_TYPE_NATIVE)
    .build()
}

/**
 * An account-defined custom asset.
 */
case class Token(code: String, issuer: AccountId) extends Asset {
  require(code.matches("^[a-zA-Z0-9]{1,12}$"), "Must be alphanumeric between 1 and 12 characters inclusive")

  override val asToken: Option[Token] = Some(this)
  private val codeBytes = new ByteString(code.getBytes(StandardCharsets.UTF_8))
  private val size = codeBytes.size()
  private val isCompact = size <= 4

  def fullCode: String = s"$code:${issuer.encodeToString}"

  override def xdrEncode: xdr.Asset = {
    val codeBytes = code.getBytes(StandardCharsets.UTF_8)
    new xdr.Asset.Builder()
      .discriminant(if (isCompact) AssetType.ASSET_TYPE_CREDIT_ALPHANUM4 else AssetType.ASSET_TYPE_CREDIT_ALPHANUM12)
      .alphaNum4(if (isCompact) new AssetAlphaNum4.Builder()
        .assetCode(new AssetCode4(codeBytes.padTo(4, 0)))
        .issuer(issuer.xdrEncode)
        .build() else null)
      .alphaNum12(if (isCompact) null else new AssetAlphaNum12.Builder()
        .assetCode(new AssetCode12(codeBytes.padTo(12, 0)))
        .issuer(issuer.xdrEncode)
        .build())
      .build()
  }

}

object Token {
  def decode(asset: xdr.Asset): Token = Asset.decode(asset).asInstanceOf[Token]
}
