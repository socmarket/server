package socmarket.twoc.api.sync

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class SyncCategoryReq(
  id       : Int,
  parentId : Option[Int],
  title    : Option[String],
  notes    : Option[String],
)

object SyncCategoryReq {
  implicit val decoder: Decoder[SyncCategoryReq] = deriveDecoder
}
