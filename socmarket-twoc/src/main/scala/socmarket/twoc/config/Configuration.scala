package socmarket.twoc.config

import cats.effect.{Async, Blocker, Concurrent, ContextShift, Resource}
import doobie.hikari.HikariTransactor
import skunk.Session

import scala.concurrent.ExecutionContext

final case class NexmoConf(sendSmsUrl: String, apiKey: String, apiSecret: String, from: String)
final case class HttpClientConf(opt: String)
final case class HttpConf(host: String, port: Int, path: String, timeout: Int, client: HttpClientConf)
final case class DbPoolConf(max: Int)
final case class DbConf(
  host: String,
  port: Int,
  database: String,
  user: String,
  password: String,
  debug: Boolean,
  pool: DbPoolConf
)
final case class ApiConfAuth(codeLen: Int)
final case class ApiConf(limits: ApiConfLimits, auth: ApiConfAuth)
final case class ApiConfLimits(
  maxCodesHourIp: Int,
  maxCodesHourMsisdn: Int,
  minMinutesBetweenCodesMsisdn: Int,
)
final case class Conf(db: DbConf, http: HttpConf, api: ApiConf, nexmo: NexmoConf)

object DbConf {

  def createSkunkSession[F[_]: Concurrent: ContextShift](dbConf: DbConf): Resource[F, Resource[F, Session[F]]] = {
    import natchez.Trace.Implicits.noop
    Session.pooled(
      host = dbConf.host,
      port = dbConf.port,
      database = dbConf.database,
      user = dbConf.user,
      password = Some(dbConf.password),
      max = dbConf.pool.max,
      debug = dbConf.debug
    )
  }

  def createTransactor[F[_]: Async: ContextShift](
    dbConf: DbConf,
    connEc: ExecutionContext,
    blocker: Blocker,
  ): Resource[F, HikariTransactor[F]] = {
    ???
  }

}