package socmarket.twoc.api.sync

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class SyncUnitReq(
  id       : Int,
  title    : Option[String],
  notation : Option[String],
)

object SyncUnitReq {
  implicit val decoder: Decoder[SyncUnitReq] = deriveDecoder
}