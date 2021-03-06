package stellar.protocol

import java.nio.charset.StandardCharsets

import org.stellar.xdr
import org.stellar.xdr.Asset.{AssetAlphaNum12, AssetAlphaNum4}
import org.stellar.xdr.{AssetCode12, AssetCode4, AssetType}

sealed trait Asset {
  val code: String

  def xdrEncode: xdr.Asset
}

object Asset {
  def decode(x: xdr.Asset): Asset = {
    x.getDiscriminant match {
      case AssetType.ASSET_TYPE_NATIVE => Lumen
/*
      case AssetType.ASSET_TYPE_CREDIT_ALPHANUM4 =>
        Token(
          code = new String(x.getAlphaNum4.getAssetCode.getAssetCode4, StandardCharsets.UTF_8),
          issuer = AccountId.decode(x.getAlphaNum4.getIssuer)
        )
      case AssetType.ASSET_TYPE_CREDIT_ALPHANUM12 =>
        Token(
          code = new String(x.getAlphaNum12.getAssetCode.getAssetCode12, StandardCharsets.UTF_8),
          issuer = AccountId.decode(x.getAlphaNum12.getIssuer)
        )
*/
    }
  }
}

/**
 * The network's native asset, XLM.
 */
case object Lumen extends Asset {
  val STROOPS_PER_LUMEN = 10_000_000L

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
  require(code.length >= 1 && code.length <= 12)

  private val isCompact = code.length <= 4
  override def xdrEncode: xdr.Asset = {
    val codeBytes = code.getBytes(StandardCharsets.UTF_8)
    new xdr.Asset.Builder()
      .discriminant(if (isCompact) AssetType.ASSET_TYPE_CREDIT_ALPHANUM4 else AssetType.ASSET_TYPE_CREDIT_ALPHANUM12)
      .alphaNum4(if (isCompact) new AssetAlphaNum4.Builder()
        .assetCode(new AssetCode4(codeBytes))
        .issuer(issuer.xdrEncode)
        .build() else null)
      .alphaNum12(if (isCompact) null else new AssetAlphaNum12.Builder()
        .assetCode(new AssetCode12(codeBytes))
        .issuer(issuer.xdrEncode)
        .build())
      .build()
  }

}
