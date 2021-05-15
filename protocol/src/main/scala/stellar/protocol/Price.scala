package stellar.protocol

import org.stellar.xdr
import org.stellar.xdr.Int32

/** A ratio to represent a price */
case class Price(n: Int, d: Int) {
  def times(l: Long): Long = l * n / d

  def xdrEncode: xdr.Price = new xdr.Price.Builder()
    .d(new Int32(d))
    .n(new Int32(n))
    .build()
}

object Price {
  def decode(price: xdr.Price): Price = Price(price.getN.getInt32, price.getD.getInt32)

  /** Construct a new Price, first finding a lower denominator if one exists */
  def from(n: Long, d: Long): Price = {
    val bigN = BigInt(n)
    val bigD = BigInt(d)
    val gcd = bigN.gcd(bigD)
    Price((bigN / gcd).intValue, (bigD / gcd).intValue)
  }

}
