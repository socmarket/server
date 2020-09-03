package socmarket.twoc.api.sync

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class SyncCurrencyReq(
  id       : Int,
  title    : Option[String],
  notation : Option[String],
)

object SyncCurrencyReq {
  implicit val decoder: Decoder[SyncCurrencyReq] = deriveDecoder
}

