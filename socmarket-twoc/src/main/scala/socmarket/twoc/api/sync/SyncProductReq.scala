package socmarket.twoc.api.sync

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class SyncProductReq()

object SyncProductReq {
  implicit val codec: Codec[SyncProductReq] = deriveCodec
}