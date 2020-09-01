package socmarket.twoc

import socmarket.twoc.config._
import socmarket.twoc.db.migration.Migration
import socmarket.twoc.http.{Client, Server}
import socmarket.twoc.ext.Nexmo
import logstage.{IzLogger, LogIO}
import cats.effect._
import io.circe.config.parser
import doobie.util.ExecutionContexts

object Application extends IOApp {

  val logger = IzLogger()

  def createServer[F[_]: ContextShift: ConcurrentEffect: Timer]: Resource[F, org.http4s.server.Server[F]] = {
    implicit val log: LogIO[F] = LogIO.fromLogger[F](logger)
    for {
      smConf <- Resource.liftF(parser.decodePathF[F, Conf]("socmarket"))
      httpSp <- ExecutionContexts.cachedThreadPool[F]
      httpCp <- ExecutionContexts.cachedThreadPool[F]
      connEc <- ExecutionContexts.fixedThreadPool[F](smConf.db.connections.poolSize)
      tranEc <- ExecutionContexts.cachedThreadPool[F]
      tx <- DbConf.createTransactor(smConf.db, connEc, Blocker.liftExecutionContext(tranEc))
      _  <- Resource.liftF(Migration.migrate(tx))
      httpClient  <- Client.create(smConf.http.client, httpCp)
      nexmo       <- Nexmo.createService(httpClient, smConf.nexmo)
      authRepo    <- db.repo.Auth.createRepo(tx, smConf)
      authService <- service.Auth.createService(authRepo, nexmo)
      httpServer  <- Server.create(smConf.http, httpSp, authService)
    } yield httpServer
  }


  def run(args: List[String]): IO[ExitCode] = {
    createServer
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }

}