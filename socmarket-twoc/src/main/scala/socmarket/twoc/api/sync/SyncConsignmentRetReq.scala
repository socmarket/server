package socmarket.twoc.api.sync

import java.time.LocalDateTime

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class SyncConsignmentRetReq(
  id                : Int,
  consignmentItemId : Int,
  quantity          : Long,
  notes             : Option[String],
  returnedAt        : LocalDateTime,
)

object SyncConsignmentRetReq extends LocalDateTimeInstances {
  implicit val decoder: Decoder[SyncConsignmentRetReq] = deriveDecoder
}
