package stellar.protocol

import org.specs2.matcher.{AnyMatchers, Expectable, MatchResult, Matcher}
import stellar.protocol.xdr.{Decoder, Encodable}

trait XdrSerdeMatchers extends AnyMatchers {

  def xdrDecodeAndEncodeDiscriminated[T <: Encodable](decoder: Decoder[T]): Matcher[T] = new Matcher[T] {
    def apply[S <: T](s: Expectable[S]): MatchResult[S] = {
      val (remaining, value) = decoder.decode.run(s.value.encodeDiscriminated).value
      val ok = "encoded and decoded"
      if (remaining.nonEmpty) result(test = false, ok, "had left-over bytes", s)
      else result(value == s.value, ok, s"did not equal. expected ${s.value} got $value", s)
    }
  }

  def xdrDecodeAndEncode[T <: Encodable](decoder: Decoder[T]): Matcher[T] = new Matcher[T] {
    def apply[S <: T](s: Expectable[S]): MatchResult[S] = {
      val (remaining, value) = decoder.decode.run(s.value.encode).value
      val ok = "encoded and decoded"
      if (remaining.nonEmpty) result(test = false, ok, "had left-over bytes", s)
      else result(value == s.value, ok, s"did not equal. expected ${s.value} got $value", s)
    }
  }

}
