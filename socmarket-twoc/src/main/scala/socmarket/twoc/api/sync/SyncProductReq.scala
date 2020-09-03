package socmarket.twoc.api.sync

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class SyncProductReq(
  id         : Int,
  barcode    : String,
  code       : Option[String],
  title      : Option[String],
  notes      : Option[String],
  unitId     : Option[Int],
  categoryId : Option[Int],
  brand      : Option[String],
  model      : Option[String],
  engine     : Option[String],
  oemNo      : Option[String],
  serial     : Option[String],
  coord      : Option[String],
)

object SyncProductReq {
  implicit val decoder: Decoder[SyncProductReq] = deriveDecoder
}