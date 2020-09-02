package socmarket.twoc.http

import socmarket.twoc.config.HttpClientConf
import cats.effect.{ConcurrentEffect, Resource, Timer}
import logstage.LogIO
import org.http4s.client.blaze._
import org.http4s.client.middleware.Logger
import org.http4s.client.{Client => Http4sClient}

import scala.concurrent.ExecutionContext

object Client {

  def create[F[_]: ConcurrentEffect : Timer : LogIO](
    conf: HttpClientConf,
    ec: ExecutionContext,
  ): Resource[F, Http4sClient[F]] = {
    BlazeClientBuilder[F](ec)
      .resource
      .map(client => Logger(logHeaders = true, logBody = true)(client))
  }
}