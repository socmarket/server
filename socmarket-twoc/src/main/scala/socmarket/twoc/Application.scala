package socmarket.twoc

import environment.Environment.{AppEnvironment, appEnvironment}
import http.Server
import logstage.LogstageZIO.LogZIO
import socmarket.twoc.environment.db.migration.Migration
import zio._

object Application extends App {

  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    startAll
      .catchAll { e =>
        for {
          log <- ZIO.access[LogZIO](_.get)
          _   <- log.error(s"Critical error: $e")
        } yield ()
      }
      .provideLayer(appEnvironment)
      .exitCode
  }

  def startAll: ZIO[AppEnvironment, Throwable, Unit] = for {
    _ <- runMigration
    _ <- Server.runServer
  } yield ()

  def runMigration: ZIO[Migration, Throwable, Unit] = {
    for {
      mig <- ZIO.access[Migration](_.get)
      _   <- mig.run
    } yield ()
  }

}