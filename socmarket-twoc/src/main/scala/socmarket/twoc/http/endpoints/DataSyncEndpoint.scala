package socmarket.twoc.http.endpoints

import socmarket.twoc.adt.auth.Account
import socmarket.twoc.api.sync.SyncProductReq
import socmarket.twoc.service.DataSync

import cats.effect.Sync
import cats.syntax.functor._
import cats.syntax.flatMap._
import org.http4s.{AuthedRoutes, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware

object DataSyncEndpoint {

  import org.http4s.circe.CirceEntityDecoder._

  def router[F[_]: Sync](
    dsync: DataSync.Service[F],
    authM: AuthMiddleware[F, Account],
  ): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = Http4sDsl[F]
    import dsl._
    authM(
      AuthedRoutes.of[Account, F] {
        case req @ POST -> Root / "product" as acc =>
          for {
            syncProductReq <- req.req.as[SyncProductReq]
            _ <- dsync.syncProduct(acc, syncProductReq)
            res <- Ok("OK")
          } yield res
      }
    )
  }

}