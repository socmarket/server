package socmarket.twoc.api

import io.circe.Encoder
import io.circe.syntax._
import io.circe.generic.semiauto.deriveEncoder

sealed trait ApiError {
  def error: Boolean = true
  def msg: String
  def code: Int
}

final case class ApiErrorUnknown(msg: String = "Unknown error", code: Int = 1) extends Exception with ApiError
final case class ApiErrorExternal(msg: String = "", code: Int = 2) extends Exception with ApiError
final case class ApiErrorLimitExceeded(msg: String = "Limit Exceeded", code: Int = 3) extends Exception with ApiError

object ApiErrorUnknown {
  implicit val encoder: Encoder[ApiErrorUnknown] = deriveEncoder
}

object ApiErrorExternal {
  implicit val encoder: Encoder[ApiErrorExternal] = deriveEncoder
}

object ApiErrorLimitExceeded {
  implicit val encoder: Encoder[ApiErrorLimitExceeded] = deriveEncoder
}

object ApiError {
  implicit val errorEncoder: Encoder[ApiError] = {
    case e: ApiErrorUnknown       => e.asJson
    case e: ApiErrorExternal      => e.asJson
    case e: ApiErrorLimitExceeded => e.asJson
  }
}