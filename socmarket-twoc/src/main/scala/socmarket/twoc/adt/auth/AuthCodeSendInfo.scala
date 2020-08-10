package socmarket.twoc.adt.auth

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

final case class AuthCodeSendInfo(info: AuthCodeInfo, code: String, handle: String, provider: String)

object AuthCodeSendInfo {
  implicit val decoder: Decoder[AuthCodeSendInfo] = deriveDecoder
}