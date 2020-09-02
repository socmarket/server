package socmarket.twoc.api.sync

import io.circe.{Codec, Decoder}
import io.circe.generic.semiauto.{deriveCodec, deriveDecoder}

case class SyncProductReq(
  productId  : Int,
  barcode    : String,
  code       : String,
  title      : String,
  notes      : String,
  unitId     : Int,
  categoryId : Int,
  brand      : String,
  model      : String,
  engine     : String,
  oemno      : String,
  serial     : String,
  coord      : String,
)

object SyncProductReq {
  implicit val decoder: Decoder[SyncProductReq] = deriveDecoder
}