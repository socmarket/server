package socmarket.twoc.adt.auth

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import socmarket.twoc.api.auth.AuthCodeReq

final case class AuthCodeInfo(req: AuthCodeReq, ip: String, userAgent: String)

object AuthCodeInfo {
  implicit val decoder: Decoder[AuthCodeInfo] = deriveDecoder
}
