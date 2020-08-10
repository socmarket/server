package socmarket.twoc.adt.auth

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class AuthToken(msisdn: Long, token: String)

object AuthToken {
  implicit val codec: Codec[AuthToken] = deriveCodec
}
