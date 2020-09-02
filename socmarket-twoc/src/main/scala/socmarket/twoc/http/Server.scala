package socmarket.twoc.http

import socmarket.twoc.config.HttpConf
import socmarket.twoc.http.endpoints.{AuthEndpoint, DataSyncEndpoint, HealthEndpoint}
import socmarket.twoc.{service => sv}
import socmarket.twoc.api
import socmarket.twoc.api.{ApiError, ApiErrorAuthFailed, ApiErrorExternal, ApiErrorLimitExceeded, ApiErrorUnknown}
import io.circe.syntax._
import cats.effect.{ConcurrentEffect, Resource, Sync, Timer}
import cats.data.{Kleisli, OptionT}
import cats.data.Kleisli._
import cats.implicits.catsSyntaxApplicativeError
import cats.syntax.functor._
import cats.syntax.semigroupk._
import cats.syntax.applicativeError._
import logstage.LogIO
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.{HttpRoutes, Request}
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.{AuthMiddleware, Router, Server => Http4sServer}
import org.http4s.server.middleware.CORS.DefaultCORSConfig
import org.http4s.server.middleware.{AutoSlash, CORS, GZip, Logger, Timeout}
import socmarket.twoc.adt.auth.Account
import socmarket.twoc.service.Auth

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

object Server {

  def create[F[_]: ConcurrentEffect : Timer : LogIO](
    conf: HttpConf,
    ec: ExecutionContext,
    authService: sv.Auth.Service[F],
    dsyncService: sv.DataSync.Service[F],
  ): Resource[F, Http4sServer[F]] = {
    val viaAuth = authM(authService)
    val routes = Router(
      "/auth"   -> AuthEndpoint.router[F](authService),
      "/health" -> HealthEndpoint.router[F](),
      "/sync"   -> DataSyncEndpoint.router[F](dsyncService, viaAuth)
    )
    val corsConf = DefaultCORSConfig.copy(allowedMethods = Some(Set("GET", "POST")))
    val mkApp: HttpRoutes[F] => HttpRoutes[F] = { http: HttpRoutes[F] => AutoSlash(http) }
      .andThen(errorHandler[F](_))
      .andThen(Logger.httpRoutes[F](logHeaders = true, logBody = true))
      .andThen(CORS(_, corsConf))
      .andThen(GZip(_))
      .andThen(Timeout(conf.timeout.seconds))
    val app = Router[F](conf.path -> mkApp(routes)).orNotFound
    BlazeServerBuilder[F](ec)
      .bindHttp(conf.port, conf.host)
      .withHttpApp(app)
      .resource
  }

  private def authM[F[_] : Sync](authService: Auth.Service[F]): AuthMiddleware[F, Account] = {
    val authUser: Kleisli[OptionT[F, *], Request[F], Account] = Kleisli { req =>
      val token = req.headers
        .get(Authorization)
        .map(_.value)
        .getOrElse("")
      OptionT(authService.verifyToken(token).map(Some(_)))
    }
    AuthMiddleware(authUser)
  }


  private def errorHandler[F[_]: ConcurrentEffect : LogIO](service: HttpRoutes[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = Http4sDsl[F]
    import dsl._
    import org.http4s.circe.CirceEntityEncoder._
    Kleisli { (req: Request[F]) =>
      service(req)
        .handleErrorWith {
          case e: ApiErrorLimitExceeded =>
            OptionT(Forbidden(e.copy(msg = "limit exceeded").asJson).map(Some(_)))
          case e: ApiErrorAuthFailed =>
            OptionT(Forbidden(e.copy(msg = "authentication failed").asJson).map(Some(_)))
          case e: ApiErrorExternal =>
            OptionT(InternalServerError(ApiErrorUnknown().asJson).map(Some(_)))
          case e: ApiError =>
            OptionT(InternalServerError(ApiErrorUnknown(e.msg).asJson).map(Some(_)))
          case e: Exception =>
            OptionT(InternalServerError(ApiErrorUnknown(e.getMessage).asJson).map(Some(_)))
          case e: Throwable =>
            OptionT(InternalServerError(ApiErrorUnknown(e.getMessage).asJson).map(Some(_)))
        }
    }
  }
}