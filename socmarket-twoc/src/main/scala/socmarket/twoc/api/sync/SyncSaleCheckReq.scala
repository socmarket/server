package socmarket.twoc.api.sync

import java.time.LocalDateTime

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class SyncSaleCheckReq(
  id       : Int,
  clientId : Option[Int],
  cash     : Option[Long],
  change   : Option[Long],
  discount : Option[Long],
  closed   : Int,
  soldAt   : LocalDateTime,
)

object SyncSaleCheckReq extends LocalDateTimeInstances {
  implicit val decoder: Decoder[SyncSaleCheckReq] = deriveDecoder
}
