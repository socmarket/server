package socmarket.twoc.http.endpoints

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`User-Agent`
import org.http4s.server.Router
import socmarket.twoc.adt.auth.AuthCodeInfo
import socmarket.twoc.api.auth.{AuthCodeReq, AuthCodeVerifyReq}
import socmarket.twoc.api.sync.SyncProductReq
import socmarket.twoc.{service => sv}

final class DataSyncEndpoint[F[_]: Sync](
  authCodeSv: sv.AuthCode.Service[F]
) {

  val dsl: Http4sDsl[F] = Http4sDsl[F]

  import dsl._
  import org.http4s.circe.CirceEntityDecoder._

  private val router = HttpRoutes.of[F] {

    case req @ POST -> Root / "product" =>
      for {
        authCodeReq <- req.as[SyncProductReq]
        res         <- Ok("OK")
      } yield res

  }

  val routes: HttpRoutes[F] = Router("/sync" -> router)
}