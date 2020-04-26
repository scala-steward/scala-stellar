package stellar.protocol.xdr

import okio.ByteString

import scala.annotation.tailrec

object ByteArrays {

  def checksum(bytes: Array[Byte]): ByteString = {
    // This code calculates CRC16-XModem checksum
    // Ported from https://github.com/alexgorbatchev/node-crc, via https://github.com/stellar/java-stellar-sdk

    @tailrec
    def loop(bs: Seq[Byte], crc: Int): Int = {
      bs match {
        case h +: t =>
          var code = crc >>> 8 & 0xFF
          code ^= h & 0xFF
          code ^= code >>> 4
          var crc_ = crc << 8 & 0xFFFF
          crc_ ^= code
          code = code << 5 & 0xFFFF
          crc_ ^= code
          code = code << 7 & 0xFFFF
          crc_ ^= code
          loop(t, crc_)
        case Nil => crc
      }
    }

    val crc = loop(bytes.toIndexedSeq, 0x0000)
    new ByteString(Array(crc.toByte, (crc >>> 8).toByte))
  }

  def paddedByteArray(bs: Array[Byte], length: Int): Array[Byte] = {
    val padded = Array.ofDim[Byte](math.max(length, bs.length))
    System.arraycopy(bs, 0, padded, 0, bs.length)
    padded
  }

  def paddedByteArray(s: String, length: Int): Array[Byte] = paddedByteArray(s.getBytes("US-ASCII"), length)

  def paddedByteArrayToString(bs: Array[Byte]): String = new String(bs, "US-ASCII").split("\u0000")(0)

}
