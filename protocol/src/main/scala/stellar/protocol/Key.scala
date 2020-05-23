package stellar.protocol

import cats.data.State
import okio.ByteString
import org.apache.commons.codec.binary.Base32
import stellar.protocol.Key.codec
import stellar.protocol.xdr.{ByteArrays, Decoder, Encodable, Encode}

/**
 * A Key (also known as a StrKey, or Stellar Key) is a typed, encoded byte array.
 */
sealed trait Key {
  val kind: Byte
  val hash: ByteString
  def checksum: ByteString = ByteArrays.checksum(kind +: hash.toByteArray)
  def encodeToString: String = codec.encode(kind +: hash.toByteArray ++: checksum.toByteArray)
    .map(_.toChar).mkString
}

object Key {
  val codec = new Base32()

  def decodeFromString(key: String): ByteString = decodeFromChars(key.toList)
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
  }

/*
  def decodeMuxed(key: List[Char]): (ByteString, Long) = {
    assert(key.forall(_ <= 127), s"Illegal characters in provided key")
    val decoded: Array[Byte] = codec.decode(key.map(_.toByte).toArray)

    val (data, Array(sumA, sumB)) = decoded.tail.splitAt(decoded.length - 3)
    val Array(checkA, checkB) = ByteArrays.checksum(decoded.take(decoded.length - 2)).toByteArray
    assert((checkA, checkB) == (sumA, sumB),
      f"Checksum does not match. Provided: 0x$sumA%04X,0x$sumB%04X. Actual: 0x$checkA%04X,0x$checkB%04X")
    val (accountId, hash) = data.splitAt(8)
    (new ByteString(hash), ByteBuffer.wrap(accountId).getLong())
  }
*/
}

/**
 * Only a subset of Keys can be signers. Seeds should not be the declared signer
 * (as they are the private dual of the AccountId).
 */
sealed trait SignerKey extends Key with Encodable

object SignerKey extends Decoder[SignerKey] {
  override val decode: State[Seq[Byte], SignerKey] = switch(
    byteString(32).map(AccountId(_)),
    byteString(32).map(PreAuthTx(_)),
    byteString(32).map(HashX(_))
  )
}

/**
 * The public facing identifier of a stellar key pair. The string encoded form starts with
 * a G. If the account includes a subAccountId then the encoded form may start with an M and
 * include that sub account id.
 */
case class AccountId(hash: ByteString) extends SignerKey {
  val kind: Byte = (6 << 3).toByte // G
  def encode: LazyList[Byte] = Encode.int(0) ++ Encode.bytes(32, hash)
  // TODO - check if the zero belongs in Address only.

/*
  val kind: Byte = subAccountId match {
    case None => (6 << 3).toByte // G
    case _ => (12 << 3).toByte   // M
  }
  def encode: LazyList[Byte] = subAccountId match {
    case None => Encode.int(0x000) ++ Encode.bytes(32, hash)
    case Some(id) => Encode.int(0x100) ++ Encode.long(id) ++ Encode.bytes(32, hash)
  }

  override def encodeToString: String = subAccountId match {
    case None => super.encodeToString
    case Some(id) =>
      codec.encode(kind +: Encode.long(id).toArray ++: hash.toByteArray ++: checksum.toByteArray)
        .map(_.toChar).mkString
  }

  override def checksum: ByteString = subAccountId match {
    case None => super.checksum
    case Some(id) => ByteArrays.checksum(kind +: Encode.long(id).toArray ++: hash.toByteArray)
  }
*/

  override def toString: String = s"AccountId($encodeToString)"
}

object AccountId extends Decoder[AccountId] {
  val decode: State[Seq[Byte], AccountId] = for {
    _ <- int
    bs <- byteString(32)
  } yield AccountId(bs)

  def apply(accountId: String): AccountId = AccountId(Key.decodeFromString(accountId))

  /*
  val decode: State[Seq[Byte], AccountId] = int.flatMap {
    case 0x000 => byteString(32).map(AccountId(_))
    case 0x100 => for {
      subAccountId <- long
      bs <- byteString(32)
    } yield AccountId(bs, Some(subAccountId))
  }

  def apply(accountId: String): AccountId = {
    accountId.headOption match {
      case Some('G') => AccountId(Key.decode(accountId.toList))
      case Some('M') =>
        val (hash, subAccountId) = Key.decodeMuxed(accountId.toList)
        AccountId(hash, Some(subAccountId))
      case _ => throw new AssertionError(s"AccountIds must start with 'G' or 'M'. Was $accountId")
    }
  }
*/
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

/**
 * Pre-authorized transactions keys are the hashes of yet to be transmitted transactions. Signers
 * of this kind are automatically removed from the account when the transaction is accepted by the
 * network. See https://www.stellar.org/developers/guides/concepts/multi-sig.html#pre-authorized-transaction
 */
case class PreAuthTx(hash: ByteString) extends SignerKey {
  val kind: Byte = (19 << 3).toByte // T
  def encode: LazyList[Byte] = Encode.int(1) ++ Encode.bytes(32, hash)
}

object PreAuthTx extends Decoder[PreAuthTx] {
  val decode: State[Seq[Byte], PreAuthTx] = for {
    _ <- int
    bs <- bytes(32)
  } yield PreAuthTx(new ByteString(bs.toArray))

  def apply(hash: String): PreAuthTx = {
    assert(hash.startsWith("T"))
    PreAuthTx(Key.decodeFromString(hash))
  }
}

/**
 * Arbitrary 256-byte values can be used as signatures on transactions. The hash of such value are
 * able to be used as signers. See https://www.stellar.org/developers/guides/concepts/multi-sig.html#hashx
 */
case class HashX(hash: ByteString) extends SignerKey {
  val kind: Byte = (23 << 3).toByte // X
  def encode: LazyList[Byte] = Encode.int(2) ++ Encode.bytes(32, hash)
}

object HashX extends Decoder[HashX] {
  val decode: State[Seq[Byte], HashX] = for {
    _ <- int
    bs <- bytes(32)
  } yield HashX(new ByteString(bs.toArray))

  def apply(hash: String): HashX = {
    assert(hash.startsWith("X"))
    HashX(Key.decodeFromString(hash))
  }
}