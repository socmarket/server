package socmarket.twoc.environment

import doobie.util.fragment.Fragment
import socmarket.twoc.environment.config.Configuration.DbConfig
import doobie.util.transactor.Transactor
import zio._
import zio.interop.catz._
import doobie._
import doobie.implicits._
import doobie.util.query.Query
import logstage.LogBIO3
import logstage.LogstageZIO.LogZIO

package object db {

  type Database = Has[Database.Service]

  object Database {
    trait Service {
      val tx: Transactor[Task]
      val log: LogBIO3[ZIO]
      def exec(sql: String): ConnectionIO[Unit] = {
        Fragment.const(sql)
          .update
          .run
          .map(_ => ())
      }
      def exec(sql: Fragment): ConnectionIO[Unit] = {
        sql
          .update
          .run
          .map(_ => ())
      }
      def selectOne[A: Read](sql: Fragment): ConnectionIO[A] = {
        sql.query[A]
          .unique
      }
    }

    val postgres: URLayer[Has[DbConfig] with LogZIO, Database] =
      ZLayer.fromServices[DbConfig, LogBIO3[ZIO], Database.Service] {
        (db, logger) =>
          new Service {
            val tx: Transactor[Task] = Transactor.fromDriverManager(db.driver, db.url, db.user, db.password)
            val log: LogBIO3[ZIO] = logger
          }
      }
  }

}