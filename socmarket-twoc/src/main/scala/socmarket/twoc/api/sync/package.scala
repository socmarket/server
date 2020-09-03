package socmarket.twoc.api

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import io.circe.{Decoder, Encoder}

package object sync {

  trait LocalDateTimeInstances {
    val localDateTimePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    implicit final val ldtDecoder: Decoder[LocalDateTime] = {
      Decoder.decodeLocalDateTimeWithFormatter(localDateTimePattern)
    }
    implicit final val ldtEncoder: Encoder[LocalDateTime] = {
      Encoder.encodeLocalDateTimeWithFormatter(localDateTimePattern)
    }
  }

}
