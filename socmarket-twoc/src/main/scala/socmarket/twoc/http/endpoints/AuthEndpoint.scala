package socmarket.twoc.http.endpoints

import socmarket.twoc.{service => sv}
import socmarket.twoc.api.auth.{AuthCodeReq, AuthCodeVerifyReq}
import socmarket.twoc.adt.auth.AuthCodeInfo

import cats.effect.Sync
import cats.syntax.functor._
import cats.syntax.flatMap._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`User-Agent`

object AuthEndpoint {

  import org.http4s.circe.CirceEntityDecoder._

  def router[F[_]: Sync](authService: sv.Auth.Service[F]): HttpRoutes[F] = {

    val dsl: Http4sDsl[F] = Http4sDsl[F]
    import dsl._

    HttpRoutes.of[F] {

      case req @ POST -> Root / "sendCode" =>
        for {
          authCodeReq <- req.as[AuthCodeReq]
          ip           = req.from.map(_.getHostAddress).getOrElse("")
          ua           = req.headers.get(`User-Agent`).fold("")(_.value)
          _           <- authService.sendCode(AuthCodeInfo(authCodeReq, ip, ua))
          res         <- Ok("OK")
        } yield res

      case req @ POST -> Root / "verify" =>
        for {
          authCodeVerifyReq <- req.as[AuthCodeVerifyReq]
          authToken         <- authService.verifyCode(authCodeVerifyReq)
          res               <- Ok(authToken.asJson)
        } yield res
    }
  }

}