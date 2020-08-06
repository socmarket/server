package socmarket.twoc.config

import cats.effect.{Async, Blocker, ContextShift, Resource}
import doobie.hikari.HikariTransactor
import scala.concurrent.ExecutionContext

final case class HttpConf(host: String, port: Int, path: String)
final case class DbPoolConf(poolSize: Int)
final case class DbConf(driver: String, url: String, user: String, password: String, connections: DbPoolConf)
final case class Conf(db: DbConf, http: HttpConf)

object DbConf {

  def createTransactor[F[_]: Async: ContextShift](
    dbConf: DbConf,
    connEc: ExecutionContext,
    blocker: Blocker,
  ): Resource[F, HikariTransactor[F]] = {
    HikariTransactor
      .newHikariTransactor[F](dbConf.driver, dbConf.url, dbConf.user, dbConf.password, connEc, blocker)
  }

}