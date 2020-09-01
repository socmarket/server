package socmarket.twoc.http.endpoints

import socmarket.twoc.api.health.HealthCheckRes

import cats.{Defer, Monad}
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

object HealthEndpoint {

  def router[F[_]: Monad : Defer](): HttpRoutes[F] = {

    val dsl: Http4sDsl[F] = Http4sDsl[F]
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / "check" =>
        Ok(HealthCheckRes(data = "HEALTH OK", apiVersion="0.0.1-beta.9").asJson)
    }
  }

}