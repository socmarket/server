package socmarket.twoc.api.auth

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

final case class AuthCodeReq(msisdn: Long, captcha: String, fingerprint: String)

object AuthCodeReq {
  implicit val decoder: Decoder[AuthCodeReq] = deriveDecoder
}