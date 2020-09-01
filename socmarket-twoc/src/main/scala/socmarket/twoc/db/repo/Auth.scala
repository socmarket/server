package socmarket.twoc.db.repo

import socmarket.twoc.config.Conf
import socmarket.twoc.api.{ApiErrorAuthFailed, ApiErrorLimitExceeded}
import socmarket.twoc.adt.auth.{Account, AuthCodeInfo, AuthCodeSendInfo}
import cats.data.OptionT
import logstage.LogIO
import doobie.{Query0, Transactor, Update0}
import doobie.implicits._
import cats.effect.{ConcurrentEffect, Resource}
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.applicativeError._

object Auth {

  trait Repo[F[_]] {
    def genCode(req: AuthCodeInfo): F[String]
    def insertSendCodeReq(req: AuthCodeInfo): F[Unit]
    def insertSendCodeRes(req: AuthCodeSendInfo): F[Unit]
    def ensureCanSendCode(req: AuthCodeInfo): F[Unit]
    def verifyCodeAndGenToken(msisdn: Long, code: String): F[String]
    def verifyToken(token: String): F[Account]
    def updateToken(token: String): F[Unit]
    def createAccount(msisdn: Long): F[Unit]
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
      insertSendCodeReqSql(req).run.transact(tx).void
    }

    def insertSendCodeRes(sendInfo: AuthCodeSendInfo): F[Unit] = {
      insertSendCodeResSql(sendInfo).run.transact(tx).void
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

    def verifyCodeAndGenToken(msisdn: Long, code: String): F[String] = {
      for {
        _     <- codeIsValidSql(msisdn, code).unique.transact(tx)
                   .ifM(F.unit, ApiErrorAuthFailed().raiseError[F, Unit])
        _     <- verifyCode(msisdn, code).run.transact(tx)
        token <- genToken(msisdn)
      } yield token
    }

    def verifyToken(token: String): F[Account] = {
      OptionT(selectAccountByToken(token)
        .option
        .transact(tx)
      ).getOrElseF(ApiErrorAuthFailed("Wrong token").raiseError[F, Account])
    }

    def updateToken(token: String): F[Unit] = {
      for {
        _ <- updateTokenSql(token).run.transact(tx)
      } yield ()
    }

    def createAccount(msisdn: Long): F[Unit] = {
      insertAccountSql(msisdn).run.transact(tx).void
    }

    private def genToken(msisdn: Long): F[String] = {
      for {
        token <- F.delay(R.nextString(conf.api.auth.tokenLen))
        _     <- insertToken(msisdn, token).run.transact(tx)
      } yield token
    }
  }

  private
  def insertSendCodeReqSql(req: AuthCodeInfo): Update0 = fr"""
    insert into auth_code_req(msisdn, captcha, ip, user_agent, fingerprint)
    values(${req.req.msisdn}, ${req.req.captcha}, ${req.ip}, ${req.userAgent}, ${req.req.fingerprint})
  """.update

  private
  def insertSendCodeResSql(sendInfo: AuthCodeSendInfo): Update0 = fr"""
    insert into auth_code(msisdn, code, provider, handle)
    values(
      ${sendInfo.info.req.msisdn},
      ${sendInfo.code},
      ${sendInfo.provider},
      ${sendInfo.handle}
    )
  """.update

  private
  def sentCodesCountByIpSql(ip: String): Query0[Int] = fr"""
    select
      coalesce(count(id), 0) as cnt
    from auth_code
    where
      ip = $ip and sent_at >= (utcnow() - interval '1 hours')
  """.query[Int]

  private
  def sentCodesCountByMsisdnSql(msisdn: Long): Query0[(Int, Int)] = fr"""
    select
      coalesce(count(id), 0) as cnt,
      coalesce(round(extract(epoch from utcnow() - max(sent_at)) / 60)::integer, 1000)
        as minutesSinceLast
    from auth_code
    where
      sent_at >= (utcnow() - interval '1 hours') and msisdn = $msisdn
  """.query[(Int, Int)]

  private
  def codeIsValidSql(msisdn: Long, code: String): Query0[Boolean] = fr"""
    select
      coalesce(count(id), 0) > 0
    from auth_code
    where
      sent_at >= (utcnow() - interval '5 minutes')
      and msisdn = $msisdn
      and code   = $code
      and verified_at is null
      and id in (select max(id) from auth_code where msisdn = $msisdn and code = $code)
  """.query[Boolean]

  private
  def verifyCode(msisdn: Long, code: String): Update0 = fr"""
    update auth_code set verified_at = utcnow() where msisdn = $msisdn and code = $code
  """.update

  private
  def insertToken(msisdn: Long, token: String): Update0 = fr"""
    insert into auth_token(msisdn, token) values($msisdn, $token)
  """.update

  private
  def updateTokenSql(token: String): Update0 = fr"""
    update auth_token set last_used_at = utcnow()
    where token = $token
  """.update

  private def insertAccountSql(msisdn: Long): Update0 =
    sql"""
       insert into account(msisdn) values(
         $msisdn
       ) on conflict do nothing
    """
      .update

  private def selectAccountByToken(token: String): Query0[Account] =
    sql"""
       select
         account.id,
         account.msisdn
       from
         auth_token
         left join account on account.msisdn = auth_token.msisdn
       where
         auth_token.token = $token
    """
      .query[Account]

}