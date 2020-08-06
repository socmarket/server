package socmarket.twoc.api.health

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class HealthCheckRes(data: String, apiVersion: String)

object HealthCheckRes {
  implicit val decoder: Decoder[HealthCheckRes] = deriveDecoder
  implicit val encoder: Encoder[HealthCheckRes] = deriveEncoder
}