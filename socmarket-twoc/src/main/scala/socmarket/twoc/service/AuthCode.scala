package socmarket.twoc.service

import socmarket.twoc.db.{repo => db}
import socmarket.twoc.api.ApiErrorLimitExceeded
import socmarket.twoc.ext.Nexmo
import cats.effect.{ConcurrentEffect, Resource}
import cats.effect.syntax.bracket._
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.applicativeError._
import logstage.LogIO
import logstage.LogIO.log
import socmarket.twoc.adt.auth.AuthCodeInfo

object AuthCode {

  trait Service[F[_]] {
    def sendCode(req: AuthCodeInfo): F[Unit]
    def verifyReg(code: String): F[Unit]
  }

  def createService[F[_]: ConcurrentEffect: LogIO](
    authCodeRepo: db.AuthCode.Repo[F],
    nexmo: Nexmo.Service[F],
  ): Resource[F, Service[F]] =
    Resource.make(ConcurrentEffect[F].delay(create(authCodeRepo, nexmo)))(_ => ConcurrentEffect[F].delay(()))

  private def create[F[_]: ConcurrentEffect : LogIO](
    authCodeRepo: db.AuthCode.Repo[F],
    nexmo: Nexmo.Service[F]
  ): Service[F] = new Service[F] {

    def sendCode(req: AuthCodeInfo): F[Unit] = {
      val action = for {
        _    <- authCodeRepo
                  .ensureCanSendCode(req)
                  .guarantee(authCodeRepo.insertSendCodeReq(req))
        code <- authCodeRepo.genCode(req)
        _    <- nexmo.sendSms(req.req.msisdn, s"SocMarket: $code")
      } yield ()
      action
        .onError {
          case ApiErrorLimitExceeded(msg, _) =>
            log.warn(s"Send code blocked for ${req.ip} ${req.req.msisdn} reason $msg")
        }
        .void
    }

    override def verifyReg(code: String): F[Unit] = {
      ???
    }
  }
}