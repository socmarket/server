package socmarket.twoc.http

import socmarket.twoc.config.HttpConf
import socmarket.twoc.http.endpoints.{AuthEndpoint, HealthEndpoint}
import socmarket.twoc.{service => sv}
import socmarket.twoc.api
import socmarket.twoc.api.{ApiError, ApiErrorExternal, ApiErrorLimitExceeded, ApiErrorUnknown}
import io.circe.syntax._
import cats.effect.{ConcurrentEffect, Resource, Timer}
import cats.data.{Kleisli, OptionT}
import cats.data.Kleisli._
import cats.implicits.catsSyntaxApplicativeError
import cats.syntax.functor._
import cats.syntax.semigroupk._
import cats.syntax.applicativeError._
import logstage.LogIO
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Request}
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.CORS.DefaultCORSConfig
import org.http4s.server.middleware.{AutoSlash, CORS, GZip, RequestLogger, Timeout}
import org.http4s.server.{Server => Http4sServer}
import tsec.authentication.{JWTAuthenticator, SecuredRequestHandler}
import tsec.mac.jca.HMACSHA256

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

object Server {

  def create[F[_]: ConcurrentEffect : Timer : LogIO](
    conf: HttpConf,
    ec: ExecutionContext,
    authCodeService: sv.AuthCode.Service[F]
  ): Resource[F, Http4sServer[F]] = {
    val corsConf = DefaultCORSConfig.copy(allowedMethods = Some(Set("GET", "POST")))
    val authRoutes = new AuthEndpoint[F](authCodeService).routes
    val healthRoutes = new HealthEndpoint[F].routes
    val routes = healthRoutes <+> authRoutes
    val app: HttpRoutes[F] => HttpRoutes[F] = { http: HttpRoutes[F] =>
        AutoSlash(http)
      }
      .andThen(errorHandler[F](_))
      .andThen(RequestLogger.httpRoutes[F](logHeaders = true, logBody = true))
      .andThen(CORS(_, corsConf))
      .andThen(GZip(_))
      .andThen(Timeout(conf.timeout.seconds))
    val router = Router[F](conf.path -> app(routes)).orNotFound
    BlazeServerBuilder[F](ec)
      .bindHttp(conf.port, conf.host)
      .withHttpApp(router)
      .resource
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