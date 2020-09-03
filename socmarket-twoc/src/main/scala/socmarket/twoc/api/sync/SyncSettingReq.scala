package socmarket.twoc.api.sync

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class SyncSettingReq(
  id    : Int,
  key   : Option[String],
  value : Option[String],
)

object SyncSettingReq {
  implicit val decoder: Decoder[SyncSettingReq] = deriveDecoder
}



