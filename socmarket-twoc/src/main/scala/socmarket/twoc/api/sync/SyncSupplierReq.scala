package socmarket.twoc.api.sync

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class SyncSupplierReq(
  id         : Int,
  name       : Option[String],
  contacts   : Option[String],
  notes      : Option[String],
)

object SyncSupplierReq {
  implicit val decoder: Decoder[SyncSupplierReq] = deriveDecoder
}



