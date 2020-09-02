package socmarket.twoc.service

import socmarket.twoc.db.{repo => db}
import socmarket.twoc.api.ApiErrorLimitExceeded
import socmarket.twoc.ext.{Nexmo, SmsPro}
import socmarket.twoc.adt.auth.{Account, AuthCodeInfo, AuthCodeSendInfo, AuthToken}
import socmarket.twoc.api.auth.AuthCodeVerifyReq
import cats.effect.{ConcurrentEffect, Resource}
import cats.effect.syntax.bracket._
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.applicativeError._
import logstage.LogIO
import logstage.LogIO.log

object Auth {

  trait Service[F[_]] {
    def sendCode(req: AuthCodeInfo): F[Unit]
    def verifyCode(req: AuthCodeVerifyReq): F[AuthToken]
    def verifyToken(token: String): F[Account]
    def createAccount(msisdn: Long): F[Unit]
  }

  def createService[F[_]: ConcurrentEffect: LogIO](
    authRepo: db.Auth.Repo[F],
    nexmo: Nexmo.Service[F],
    smsPro: SmsPro.Service[F],
  ): Resource[F, Service[F]] = {
    Resource.make(
      ConcurrentEffect[F].delay(create(authRepo, nexmo, smsPro))
    )(
      _ => ConcurrentEffect[F].delay(())
    )
  }

  private def create[F[_]: ConcurrentEffect : LogIO](
    authRepo: db.Auth.Repo[F],
    nexmo: Nexmo.Service[F],
    smsPro: SmsPro.Service[F],
  ): Service[F] = new Service[F] {

    private val F = implicitly[ConcurrentEffect[F]]

    def sendCode(req: AuthCodeInfo): F[Unit] = {
      val action = for {
        id    <- authRepo.insertSendCodeReq(req)
        _     <- authRepo.ensureCanSendCode(req)
        code  <- authRepo.genCode(req)
        // msgId <- smsPro.sendSms(id, req.req.msisdn, s"SocMarket: $code")
        msgId = id.toString
        _     <- authRepo.insertSendCodeRes(AuthCodeSendInfo(req, code, msgId, "smspro"))
      } yield ()
      action
        .onError {
          case ApiErrorLimitExceeded(msg, _) =>
            log.warn(s"Send code blocked for ${req.ip} ${req.req.msisdn} reason $msg")
        }
    }

    def verifyCode(req: AuthCodeVerifyReq): F[AuthToken] = {
      authRepo
        .verifyCodeAndGenToken(req.msisdn, req.code)
        .flatTap(_ => authRepo.createAccount(req.msisdn))
        .map(token => AuthToken(req.msisdn, token))
    }

    override def verifyToken(token: String): F[Account] = {
      authRepo.verifyToken(token)
    }

    override def createAccount(msisdn: Long): F[Unit] = {
      authRepo.createAccount(msisdn)
    }
  }
}