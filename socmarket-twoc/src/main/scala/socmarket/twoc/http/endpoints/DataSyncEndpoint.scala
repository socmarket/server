package socmarket.twoc.http.endpoints

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.circe.syntax._
import org.http4s.{AuthedRoutes, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware
import socmarket.twoc.adt.auth.Account
import socmarket.twoc.api.sync.SyncProductReq

object DataSyncEndpoint {

  import org.http4s.circe.CirceEntityDecoder._

  def router[F[_]: Sync](authM: AuthMiddleware[F, Account]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = Http4sDsl[F]
    import dsl._
    authM(
      AuthedRoutes.of[Account, F] {
        case req @ POST -> Root / "product" as acc =>
          for {
            res <- Ok("OK")
          } yield res
      }
    )
  }

}