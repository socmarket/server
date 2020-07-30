package socmarket.twoc.environment.db.migration

import socmarket.twoc.environment.db.Database
import socmarket.twoc.environment.db.migration.steps.{Meta, Step}

import java.time.ZoneOffset
import logstage.LogBIO3
import zio.clock.Clock
import zio.{Task, URLayer, ZIO, ZLayer}
import logstage.LogstageZIO.LogZIO
import doobie.implicits._
import doobie.implicits.javatime._
import tofu.doobie.instances._
import tofu.zioInstances._

object Migration {

  trait Service {
    def run: Task[Unit]
  }

  val live: URLayer[Clock with Database with LogZIO, Migration] =
    ZLayer.fromServices[Clock.Service, Database.Service, LogBIO3[ZIO], Migration.Service] {
      (clock, db, log) =>
        new Service {
          def run: Task[Unit] = migrate(db, clock, log)
        }
    }

  private def migrate(db: Database.Service, clock: Clock.Service, log: LogBIO3[ZIO]): Task[Unit] = {
    getLastKey(db)
      .catchAll(error =>
        for {
          _ <- log.error(s"Can't get migration last key: $error")
          _ <- log.warn("Assuming migrations table does not exist, creating")
          _ <- createMig(db, log)
          lastKey <- getLastKey(db)
        } yield lastKey
      )
      .flatMap(lastKey => applyDiff(lastKey.getOrElse(""), db, clock, log))
      .unit
  }

  private def applyDiff(lastKey: String, db: Database.Service, clock: Clock.Service, log: LogBIO3[ZIO]): Task[Unit] = {
    val diffs = steps.list()
      .filter(_.key > lastKey)
      .map(mkStep(_, db, clock, log))
    val diffsKeys = steps.list().filter(_.key > lastKey).map(_.key)
    val m = for {
      _ <- log.info(s"Starting migration, steps to apply: $diffsKeys")
      _ <- db.exec("begin")
      _ <- ZIO.collectAll_(diffs)
      _ <- db.exec("commit")
      _ <- log.info(s"Database migrated successfully")
    } yield ()
    m.flatMapError{ e =>
      for {
        _ <- log.error(s"Error while running migration step: $e")
        _ <- db.exec("rollback").catchAll(ee => log.error(s"Can't rollback migration $ee"))
        _ <- log.debug(s"Rolled back migration changes")
      } yield new RuntimeException("Failed to apply migration")
    }
  }

  private def getLastKey(db: Database.Service): Task[Option[String]] = {
    db.selectOne[Option[String]](sql"select max(mkey) as mkey from migration")
  }

  private def createMig(db: Database.Service, log: LogBIO3[ZIO]): Task[Unit] = {
    for {
      _ <- log.debug("Creating migrations table").lift
      _ <- db.exec("create table migration(mkey varchar primary key, applied_at timestamp not null)")
      _ <- log.debug("Table migration created")
    } yield ()
  }

  private def mkStep(step: Step, db: Database.Service, clock: Clock.Service, log: LogBIO3[ZIO]): Task[Unit] = {
    for {
      offsetDt <- clock.currentDateTime
      dt       = offsetDt.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime
      _        <- log.debug(s"Running migration step: ${step.key}")
      _        <- step.run(Meta(db, offsetDt))
      _        <- db.exec(fr"insert into migration(mkey, applied_at) values(${step.key}, $dt)")
      _        <- log.debug(s"Applied migration step: ${step.key}")
    } yield ()
  }
}