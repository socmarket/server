package socmarket.twoc.api.auth

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

final case class AuthCodeVerifyReq(msisdn: Long, code: String)

object AuthCodeVerifyReq {
  implicit val decoder: Decoder[AuthCodeVerifyReq] = deriveDecoder
}