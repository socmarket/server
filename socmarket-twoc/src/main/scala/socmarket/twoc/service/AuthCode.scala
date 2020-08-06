package socmarket.twoc.service

import socmarket.twoc.db.{repo => db}
import socmarket.twoc.api.auth.AuthCodeReq

import cats.effect.{ConcurrentEffect, Resource}

object AuthCode {

  trait Service[F[_]] {
    def sendCode(req: AuthCodeReq, ip: String, userAgent: String): F[Unit]
  }

  def createService[F[_]: ConcurrentEffect](authCodeRepo: db.AuthCode.Repo[F]): Resource[F, Service[F]] =
    Resource.make(ConcurrentEffect[F].delay(create(authCodeRepo)))(_ => ConcurrentEffect[F].delay(()))

  private def create[F[_]: ConcurrentEffect](authCodeRepo: db.AuthCode.Repo[F]): Service[F] = new Service[F] {
    def sendCode(req: AuthCodeReq, ip: String, userAgent: String): F[Unit] = {
      authCodeRepo.insertSendCodeReq(req, ip, userAgent)
    }
  }
}