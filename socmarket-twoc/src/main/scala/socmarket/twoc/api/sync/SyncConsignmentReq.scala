package socmarket.twoc.api.sync

import java.time.LocalDateTime

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class SyncConsignmentReq(
  id         : Int,
  supplierId : Option[Int],
  closed     : Int,
  acceptedAt : LocalDateTime,
)

object SyncConsignmentReq extends LocalDateTimeInstances {
  implicit val decoder: Decoder[SyncConsignmentReq] = deriveDecoder
}
