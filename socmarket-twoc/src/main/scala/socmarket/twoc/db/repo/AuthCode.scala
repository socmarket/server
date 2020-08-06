package socmarket.twoc.db.repo

import socmarket.twoc.api.auth.AuthCodeReq

import cats.effect.{ConcurrentEffect, Resource}
import doobie.{Transactor, Update0}
import doobie.implicits._
import cats.syntax.functor._

object AuthCode {

  trait Repo[F[_]] {
    def insertSendCodeReq(req: AuthCodeReq, ip: String, userAgent: String): F[Unit]
  }

  def createRepo[F[_]: ConcurrentEffect](tx: Transactor[F]): Resource[F, Repo[F]] =
    Resource.make(ConcurrentEffect[F].delay(create(tx)))(_ => ConcurrentEffect[F].delay(()))

  private def create[F[_]: ConcurrentEffect](tx: Transactor[F]): Repo[F] = new Repo[F] {

    def insertSendCodeReq(req: AuthCodeReq, ip: String, userAgent: String): F[Unit] = {
      for {
        _ <- insertReqSql(req, ip, userAgent).run.transact(tx)
      } yield ()
    }

  }

  private def insertReqSql(req: AuthCodeReq, ip: String, userAgent: String): Update0 = fr"""
    insert into auth_code_req(msisdn, captcha, ip, user_agent)
    values(${req.msisdn}, ${req.captcha}, $ip, $userAgent)
  """.update
}