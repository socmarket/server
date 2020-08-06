package socmarket.twoc.http

import socmarket.twoc.config.HttpConf
import socmarket.twoc.http.endpoints.{AuthEndpoint, HealthEndpoint}
import socmarket.twoc.{service => sv}
import cats.effect.{ConcurrentEffect, Resource, Timer}
import cats.syntax.semigroupk._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.CORS.DefaultCORSConfig
import org.http4s.server.middleware.{AutoSlash, CORS, GZip}
import org.http4s.server.{Server => Http4sServer}

import scala.concurrent.ExecutionContext

object Server {

  def create[F[_]: ConcurrentEffect : Timer](
    conf: HttpConf,
    ec: ExecutionContext,
    authCodeService: sv.AuthCode.Service[F]
  ): Resource[F, Http4sServer[F]] = {
    val authRoutes = new AuthEndpoint[F](authCodeService).routes
    val healthRoutes = new HealthEndpoint[F].routes
    val routes = healthRoutes <+> authRoutes
    val router = Router[F](
      conf.path ->
        GZip(
          CORS(AutoSlash(routes),  config = DefaultCORSConfig.copy(allowedMethods = Some(Set("GET", "POST"))))
        )
    ).orNotFound
    BlazeServerBuilder[F](ec)
      .bindHttp(conf.port, conf.host)
      .withHttpApp(router)
      .resource
  }

}