package socmarket.twoc.api.sync

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class SyncBarcodeReq(
  id   : Int,
  code : Int,
)

object SyncBarcodeReq {
  implicit val decoder: Decoder[SyncBarcodeReq] = deriveDecoder
}