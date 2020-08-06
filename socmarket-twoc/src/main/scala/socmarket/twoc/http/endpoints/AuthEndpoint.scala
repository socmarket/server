package socmarket.twoc.http.endpoints

import socmarket.twoc.{service => sv}
import socmarket.twoc.api.auth.AuthCodeReq

import cats.effect.Sync
import cats.syntax.functor._
import cats.syntax.flatMap._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`User-Agent`
import org.http4s.server.Router

final class AuthEndpoint[F[_]: Sync](
  authCodeSv: sv.AuthCode.Service[F]
) {

  val dsl: Http4sDsl[F] = Http4sDsl[F]

  import dsl._
  import org.http4s.circe.CirceEntityDecoder._

  private val router = HttpRoutes.of[F] {

    case req @ POST -> Root / "sendCode" =>
      for {
        authCodeReq <- req.as[AuthCodeReq]
        ip           = req.from.map(_.getHostAddress).getOrElse("")
        ua           = req.headers.get(`User-Agent`).fold("")(_.value)
        _           <- authCodeSv.sendCode(authCodeReq, ip, ua)
        res         <- Ok("OK")
      } yield res
  }

  val routes: HttpRoutes[F] = Router("/auth" -> router)
}