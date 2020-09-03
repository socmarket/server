package socmarket.twoc.api.sync

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class SyncConsignmentItemReq(
  id            : Int,
  consignmentId : Int,
  productId     : Int,
  unitId        : Option[Int],
  currencyId    : Option[Int],
  quantity      : Option[Long],
  price         : Option[Long],
)

object SyncConsignmentItemReq {
  implicit val decoder: Decoder[SyncConsignmentItemReq] = deriveDecoder
}