package socmarket.twoc.api.sync

import java.time.LocalDateTime

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class SyncPriceReq(
  id         : Int,
  productId  : Int,
  currencyId : Option[Int],
  price      : Option[Long],
  setAt      : LocalDateTime,
)

object SyncPriceReq extends LocalDateTimeInstances {
  implicit val decoder: Decoder[SyncPriceReq] = deriveDecoder
}