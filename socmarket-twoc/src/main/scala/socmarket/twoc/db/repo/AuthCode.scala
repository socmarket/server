package socmarket.twoc.db.repo

import socmarket.twoc.config.Conf
import socmarket.twoc.adt.auth.AuthCodeInfo
import socmarket.twoc.api.ApiErrorLimitExceeded

import logstage.LogIO
import cats.effect.{ConcurrentEffect, Resource}
import cats.syntax.functor._
import cats.syntax.flatMap._
import skunk._
import skunk.implicits._
import skunk.codec.all._

object AuthCode {

  trait Repo[F[_]] {
    def genCode(req: AuthCodeInfo): F[String]
    def insertSendCodeReq(req: AuthCodeInfo): F[Unit]
    def insertSendCodeRes(req: AuthCodeInfo, handle: String): F[Unit]
    def ensureCanSendCode(req: AuthCodeInfo): F[Unit]
  }

  def createRepo[F[_]: ConcurrentEffect: LogIO](skunkP: Resource[F, Session[F]], conf: Conf): Resource[F, Repo[F]] =
    Resource.make(ConcurrentEffect[F].delay(create(skunkP, conf)))(_ => ConcurrentEffect[F].delay(()))

  private
  def create[F[_]: ConcurrentEffect: LogIO](skunkP: Resource[F, Session[F]], conf: Conf): Repo[F] = new Repo[F] {

    private val F: ConcurrentEffect[F] = implicitly[ConcurrentEffect[F]]
    private val R = scala.util.Random
    private val codeMax: Int = Math.pow(10, conf.api.auth.codeLen).toInt

     def genCode(req: AuthCodeInfo): F[String] = {
      F.delay(R.nextInt(codeMax).toString.padTo(conf.api.auth.codeLen, '0'))
    }

    def insertSendCodeReq(req: AuthCodeInfo): F[Unit] = {
      skunkP.use { s =>
        s.prepare(insertSendCodeReqSql).use { q =>
          q.unique(req.req.msisdn ~ req.req.captcha ~ req.ip ~ req.userAgent ~ req.req.fingerprint)
        }
      }.void
    }

    override def insertSendCodeRes(req: AuthCodeInfo, handle: String): F[Unit] = {
      ???
    }

    def ensureCanSendCode(req: AuthCodeInfo): F[Unit] = {
      skunkP.use { s =>
        for {
          /*_*/
          sentByIp <- s.prepare(sentCodesCountByIpSql).use(_.unique(req.ip))
          byMsiSdn <- s.prepare(sentCodesCountByMsisdnSql).use(_.unique(req.req.msisdn))
          cond1     = (_: Unit) => sentByIp < conf.api.limits.maxCodesHourIp
          cond2     = (_: Unit) => byMsiSdn._1 < conf.api.limits.maxCodesHourMsisdn
          cond3     = (_: Unit) => byMsiSdn._2 > conf.api.limits.minMinutesBetweenCodesMsisdn
          _        <- F.ensure(F.unit)(ApiErrorLimitExceeded("ip"))(cond1)
          _        <- F.ensure(F.unit)(ApiErrorLimitExceeded("msisdn"))(cond2)
          _        <- F.ensure(F.unit)(ApiErrorLimitExceeded("msisdn interval"))(cond3)
          /*_*/
        } yield ()
      }
    }
  }

  private val insertSendCodeReqSql = sql"""
    insert into auth_code_req(msisdn, captcha, ip, user_agent, fingerprint)
    values($int8, $varchar, $varchar, $varchar, $varchar)
    returning id
  """.query(int4)

  private val sentCodesCountByIpSql = sql"""
    select
      coalesce(count(id), 0) as cnt
    from auth_code_req
    where
      requested_at >= (utcnow() - interval '1 hours') and ip = $varchar
  """.query(int4)

  private val sentCodesCountByMsisdnSql = sql"""
    select
      coalesce(count(id), 0) as cnt,
      coalesce(round(extract(epoch from utcnow() - max(requested_at)) / 60)::integer, 1000)
        as minutesSinceLast
    from auth_code_req
    where
      requested_at >= (utcnow() - interval '1 hours') and msisdn = $int8
  """.query(int4 ~ int4)

  /*
  private
  def insertSendCodeResSql(req: AuthCodeInfo, handle: String, provider: String, code: String): Query0[Int] = fr"""
    insert into auth_code(msisdn, code, provider)
    values(${req.req.msisdn}, ${req.req.captcha}, ${req.ip}, ${req.userAgent}, ${req.req.fingerprint})
    returning id
  """.query[Int]

  */

}