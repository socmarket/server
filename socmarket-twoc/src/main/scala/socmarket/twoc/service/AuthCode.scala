package socmarket.twoc.service

import socmarket.twoc.db.{repo => db}
import socmarket.twoc.api.ApiErrorLimitExceeded
import socmarket.twoc.ext.Nexmo
import socmarket.twoc.adt.auth.{AuthCodeInfo, AuthCodeSendInfo, AuthToken}
import socmarket.twoc.api.auth.{Account, AuthCodeVerifyReq}
import cats.effect.{ConcurrentEffect, Resource}
import cats.effect.syntax.bracket._
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.applicativeError._
import logstage.LogIO
import logstage.LogIO.log

object AuthCode {

  trait Service[F[_]] {
    def sendCode(req: AuthCodeInfo): F[Unit]
    def verify(req: AuthCodeVerifyReq): F[AuthToken]
  }

  def createService[F[_]: ConcurrentEffect: LogIO](
    authCodeRepo: db.AuthCode.Repo[F],
    accountRepo: db.Account.Repo[F],
    nexmo: Nexmo.Service[F],
  ): Resource[F, Service[F]] = {
    Resource.make(
      ConcurrentEffect[F].delay(create(authCodeRepo, accountRepo, nexmo))
    )(
      _ => ConcurrentEffect[F].delay(())
    )
  }

  private def create[F[_]: ConcurrentEffect : LogIO](
    authCodeRepo: db.AuthCode.Repo[F],
    accountRepo: db.Account.Repo[F],
    nexmo: Nexmo.Service[F]
  ): Service[F] = new Service[F] {

    private val F = implicitly[ConcurrentEffect[F]]

    def sendCode(req: AuthCodeInfo): F[Unit] = {
      val action = for {
        _     <- authCodeRepo
                   .ensureCanSendCode(req)
                   .guarantee(authCodeRepo.insertSendCodeReq(req))
        code  <- authCodeRepo.genCode(req)
        msg   <- nexmo.sendSms(req.req.msisdn, s"SocMarket: $code")
        _     <- authCodeRepo.insertSendCodeRes(AuthCodeSendInfo(req, code, msg.messageId, "nexmo"))
      } yield ()
      action
        .onError {
          case ApiErrorLimitExceeded(msg, _) =>
            log.warn(s"Send code blocked for ${req.ip} ${req.req.msisdn} reason $msg")
        }
    }

    def verify(req: AuthCodeVerifyReq): F[AuthToken] = {
      authCodeRepo
        .verifyAndGenToken(req.msisdn, req.code)
        .flatTap(_ => accountRepo.create(Account(req.msisdn)))
        .map(token => AuthToken(req.msisdn, token))
    }
  }
}