package socmarket.twoc.db.repo

import socmarket.twoc.config.Conf
import socmarket.twoc.api.ApiErrorLimitExceeded
import logstage.LogIO
import cats.effect.{ConcurrentEffect, Resource}
import doobie.{Query0, Transactor}
import doobie.implicits._
import cats.syntax.functor._
import cats.syntax.flatMap._
import socmarket.twoc.adt.auth.AuthCodeInfo

object AuthCode {

  trait Repo[F[_]] {
    def genCode(req: AuthCodeInfo): F[String]
    def insertSendCodeReq(req: AuthCodeInfo): F[Unit]
    def insertSendCodeRes(req: AuthCodeInfo, handle: String): F[Unit]
    def ensureCanSendCode(req: AuthCodeInfo): F[Unit]
  }

  def createRepo[F[_]: ConcurrentEffect: LogIO](tx: Transactor[F], conf: Conf): Resource[F, Repo[F]] =
    Resource.make(ConcurrentEffect[F].delay(create(tx, conf)))(_ => ConcurrentEffect[F].delay(()))

  private
  def create[F[_]: ConcurrentEffect: LogIO](tx: Transactor[F], conf: Conf): Repo[F] = new Repo[F] {

    private val F: ConcurrentEffect[F] = implicitly[ConcurrentEffect[F]]
    private val R = scala.util.Random
    private val codeMax: Int = Math.pow(10, conf.api.auth.codeLen).toInt

     def genCode(req: AuthCodeInfo): F[String] = {
      F.delay(R.nextInt(codeMax).toString.padTo(conf.api.auth.codeLen, '0'))
    }

    def insertSendCodeReq(req: AuthCodeInfo): F[Unit] = {
      for {
        _ <- insertSendCodeReqSql(req).unique.transact(tx)
      } yield ()
    }

    override def insertSendCodeRes(req: AuthCodeInfo, handle: String): F[Unit] = {
      ???
    }

    def ensureCanSendCode(req: AuthCodeInfo): F[Unit] = {
      for {
        sentByIp <- sentCodesCountByIpSql(req.ip).unique.transact(tx)
        byMsiSdn <- sentCodesCountByMsisdnSql(req.req.msisdn).unique.transact(tx)
        cond1     = (_: Unit) => sentByIp < conf.api.limits.maxCodesHourIp
        cond2     = (_: Unit) => byMsiSdn._1 < conf.api.limits.maxCodesHourMsisdn
        cond3     = (_: Unit) => byMsiSdn._2 > conf.api.limits.minMinutesBetweenCodesMsisdn
        _        <- F.ensure(F.unit)(ApiErrorLimitExceeded("ip"))(cond1)
        _        <- F.ensure(F.unit)(ApiErrorLimitExceeded("msisdn"))(cond2)
        _        <- F.ensure(F.unit)(ApiErrorLimitExceeded("msisdn interval"))(cond3)
      } yield ()
    }
  }

  private
  def insertSendCodeReqSql(req: AuthCodeInfo): Query0[Int] = fr"""
    insert into auth_code_req(msisdn, captcha, ip, user_agent, fingerprint)
    values(${req.req.msisdn}, ${req.req.captcha}, ${req.ip}, ${req.userAgent}, ${req.req.fingerprint})
    returing id
  """.query[Int]

  private
  def insertSendCodeResSql(req: AuthCodeInfo, handle: String, provider: String, code: String): Query0[Int] = fr"""
    insert into auth_code(msisdn, code, provider)
    values(${req.req.msisdn}, ${req.req.captcha}, ${req.ip}, ${req.userAgent}, ${req.req.fingerprint})
    returing id
  """.query[Int]

  private def sentCodesCountByIpSql(ip: String): Query0[Int] = fr"""
    select
      coalesce(count(id), 0) as cnt
    from auth_code_req
    where
      requested_at >= (utcnow() - interval '1 hours') and ip = $ip
  """.query[Int]

  private def sentCodesCountByMsisdnSql(msisdn: Long): Query0[(Int, Int)] = fr"""
    select
      coalesce(count(id), 0) as cnt,
      coalesce(round(extract(epoch from utcnow() - max(requested_at)) / 60)::integer, 1000)
        as minutesSinceLast
    from auth_code_req
    where
      requested_at >= (utcnow() - interval '1 hours') and msisdn = $msisdn
  """.query[(Int, Int)]

}