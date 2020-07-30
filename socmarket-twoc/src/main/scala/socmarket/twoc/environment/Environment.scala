package socmarket.twoc.environment

import izumi.logstage.api.IzLogger
import logstage.LogstageZIO
import logstage.LogstageZIO.LogZIO
import socmarket.twoc.environment.config.Configuration
import socmarket.twoc.environment.db.migration.Migration
import socmarket.twoc.environment.db.Database
import zio.{ULayer, ZLayer}
import zio.clock.Clock

object Environment {
  type HttpServerEnvironment = Configuration with Clock
  type AppEnvironment = HttpServerEnvironment with LogZIO with Migration

  val log: ULayer[LogZIO] = ZLayer.succeed(LogstageZIO.withFiberId(IzLogger()))
  val db: ULayer[Database] = Configuration.live ++ log >>> Database.postgres
  val httpServerEnvironment: ULayer[HttpServerEnvironment] = Configuration.live ++ Clock.live
  val dbMigration: ULayer[Migration] = Clock.live ++ db ++ log >>> Migration.live
  val appEnvironment: ULayer[AppEnvironment] = httpServerEnvironment ++ log ++ dbMigration
}
