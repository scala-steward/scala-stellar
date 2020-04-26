package stellar.protocol

import cats.data.State
import okio.ByteString
import org.apache.commons.codec.binary.Base32
import stellar.protocol.Key.codec
import stellar.protocol.xdr.{Decode, Encodable, Encode}

/**
 * A Key (also known as a StrKey, or Stellar Key) is a typed, encoded byte array.
 */
sealed trait Key {
  val kind: Byte
  val hash: ByteString
  def checksum: ByteString = ByteArrays.checksum(kind +: hash.toByteArray)
  def encodeToString: String = codec.encode(kind +: (hash.toByteArray ++: checksum.toByteArray))
    .map(_.toChar).mkString
}

object Key {
  val codec = new Base32()

  def decodeFromString(key: String): ByteString = decodeFromChars(key.toIndexedSeq)
  def decodeFromChars(key: Seq[Char]): ByteString = {
    assert(key.forall(_ <= 127), s"Illegal characters in provided StrKey")

    val bytes = key.map(_.toByte).toArray
    val decoded: Array[Byte] = codec.decode(bytes)
    assert(decoded.length == 35, s"Incorrect length. Expected 35 bytes, got ${decoded.length} in StrKey: $key")

    val data = decoded.tail.take(32)
    val Array(sumA, sumB) = decoded.drop(33)
    val Array(checkA, checkB) = ByteArrays.checksum(decoded.take(33)).toByteArray
    assert((checkA, checkB) == (sumA, sumB),
      f"Checksum does not match. Provided: 0x$sumA%04X0x$sumB%04X. Actual: 0x$checkA%04X0x$checkB%04X")
    new ByteString(data)
/*
    key.head match {
      case 'G' => AccountId(new ByteString(data))
//      case 'S' => Seed(data.toIndexedSeq)
//      case 'T' => PreAuthTx(data.toIndexedSeq)
//      case 'X' => SHA256Hash(data.toIndexedSeq)
    }
*/
  }

}

/**
 * The public facing identifier of a stellar key pair. The string encoded form always starts with a G.
 */
case class AccountId(hash: ByteString) extends Key with Encodable {
  val kind: Byte = (6 << 3).toByte // G
  override def encode: LazyList[Byte] = Encode.int(0) ++ Encode.bytes(32, hash.toByteArray)
}

object AccountId extends Decode {
  val decode: State[Seq[Byte], AccountId] = for {
    _ <- int
    bs <- bytes(32)
  } yield AccountId(new ByteString(bs.toArray))

  def apply(accountId: String): AccountId = {
    assert(accountId.startsWith("G"))
    AccountId(Key.decodeFromString(accountId))
  }
}

/**
 * The private dual of the account id. Seeds are not encodable, because they are never transmitted.
 */
case class Seed(hash: ByteString) extends Key {
  val kind: Byte = (18 << 3).toByte // S
}

object Seed {
  def apply(secret: String): Seed = {
    assert(secret.startsWith("S"))
    Seed(Key.decodeFromString(secret))
  }
}