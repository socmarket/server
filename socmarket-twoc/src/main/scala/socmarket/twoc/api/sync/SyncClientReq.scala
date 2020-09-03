package socmarket.twoc.api.sync

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class SyncClientReq(
  id         : Int,
  name       : Option[String],
  contacts   : Option[String],
  notes      : Option[String],
)

object SyncClientReq {
  implicit val decoder: Decoder[SyncClientReq] = deriveDecoder
}

