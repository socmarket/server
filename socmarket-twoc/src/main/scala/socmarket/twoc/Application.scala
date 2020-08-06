package socmarket.twoc

import socmarket.twoc.config._
import socmarket.twoc.db.{repo => db}
import socmarket.twoc.{service => service}
import socmarket.twoc.db.migration.Migration
import socmarket.twoc.http.Server

import cats.effect._
import io.circe.config.parser
import doobie.util.ExecutionContexts

object Application extends IOApp {

  def createServer[F[_]: ContextShift: ConcurrentEffect: Timer]: Resource[F, org.http4s.server.Server[F]] =
    for {
      smConf <- Resource.liftF(parser.decodePathF[F, Conf]("socmarket"))
      httpEc <- ExecutionContexts.cachedThreadPool[F]
      connEc <- ExecutionContexts.fixedThreadPool[F](smConf.db.connections.poolSize)
      tranEc <- ExecutionContexts.cachedThreadPool[F]
      tx <- DbConf.createTransactor(smConf.db, connEc, Blocker.liftExecutionContext(tranEc))
      _  <- Resource.liftF(Migration.migrate(tx))
      authCodeRepo    <- db.AuthCode.createRepo(tx)
      authCodeService <- service.AuthCode.createService(authCodeRepo)
      httpServer <- Server.create(smConf.http, httpEc, authCodeService)
    } yield httpServer

  def run(args: List[String]): IO[ExitCode] = {
    createServer
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }

}