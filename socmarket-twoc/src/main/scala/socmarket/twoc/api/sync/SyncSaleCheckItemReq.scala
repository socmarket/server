package socmarket.twoc.api.sync

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class SyncSaleCheckItemReq(
  id            : Int,
  saleCheckId   : Int,
  productId     : Int,
  unitId        : Option[Int],
  currencyId    : Option[Int],
  quantity      : Option[Long],
  originalPrice : Option[Long],
  price         : Option[Long],
  discount      : Option[Long],
)

object SyncSaleCheckItemReq {
  implicit val decoder: Decoder[SyncSaleCheckItemReq] = deriveDecoder
}


