package socmarket.twoc.api.sync

import java.time.LocalDateTime

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class SyncSaleCheckRetReq(
  id              : Int,
  saleCheckItemId : Int,
  quantity        : Long,
  notes           : Option[String],
  returnedAt      : LocalDateTime,
)

object SyncSaleCheckRetReq extends LocalDateTimeInstances {
  implicit val decoder: Decoder[SyncSaleCheckRetReq] = deriveDecoder
}
