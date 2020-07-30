package socmarket.twoc.http

import socmarket.twoc.environment.Environment.AppEnvironment
import socmarket.twoc.environment.config.Configuration.HttpServerConfig
import socmarket.twoc.http.endpoints.HealthEndpoint
import cats.data.Kleisli
import cats.effect.ExitCode
import cats.implicits._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.{ AutoSlash, GZip }
import org.http4s.{ HttpRoutes, Request, Response }
import zio.interop.catz._
import zio.{ RIO, ZIO }

object Server {
  type ServerRIO[A] = RIO[AppEnvironment, A]
  type ServerRoutes = Kleisli[ServerRIO, Request[ServerRIO], Response[ServerRIO]]

  def runServer: ZIO[AppEnvironment, Throwable, Unit] =
    ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
      val cfg = rts.environment.get[HttpServerConfig]
      val ec = rts.platform.executor.asEC

      BlazeServerBuilder[ServerRIO](ec)
        .bindHttp(cfg.port, cfg.host)
        .withHttpApp(createRoutes(cfg.path))
        .serve
        .compile[ServerRIO, ServerRIO, ExitCode]
        .drain
    }

  def createRoutes(basePath: String): ServerRoutes = {
    val healthRoutes = new HealthEndpoint[AppEnvironment].routes
    val routes = healthRoutes

    Router[ServerRIO](basePath -> middleware(routes)).orNotFound
  }

  private val middleware: HttpRoutes[ServerRIO] => HttpRoutes[ServerRIO] = {
    { http: HttpRoutes[ServerRIO] =>
      AutoSlash(http)
    }.andThen { http: HttpRoutes[ServerRIO] =>
      GZip(http)
    }
  }
}
