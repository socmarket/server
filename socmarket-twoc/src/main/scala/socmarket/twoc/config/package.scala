package socmarket.twoc

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

package object config {
  implicit val httpConfDecoder: Decoder[HttpConf] = deriveDecoder
  implicit val dbConfDecoder: Decoder[DbConf] = deriveDecoder
  implicit val dbPoolConfDecoder: Decoder[DbPoolConf] = deriveDecoder
  implicit val confDecoder: Decoder[Conf] = deriveDecoder
}