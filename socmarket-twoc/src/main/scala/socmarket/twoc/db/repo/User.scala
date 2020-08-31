package socmarket.twoc.db.repo

import socmarket.twoc.api
import socmarket.twoc.config.Conf
import cats.syntax.functor._
import cats.data.OptionT
import cats.effect.{ConcurrentEffect, Resource}
import doobie.implicits._
import doobie.{Query0, Transactor, Update0}
import logstage.LogIO
import tsec.authentication.IdentityStore

object User {

  trait Repo[F[_]] extends IdentityStore[F, Long, api.auth.User] {
    def create(user: api.auth.User): F[Unit]
    def get(userId: Long): OptionT[F, api.auth.User]
  }

  def createRepo[F[_] : ConcurrentEffect : LogIO](tx: Transactor[F], conf: Conf): Resource[F, Repo[F]] =
    Resource.make(ConcurrentEffect[F].delay(create(tx, conf)))(_ => ConcurrentEffect[F].delay(()))

  private
  def create[F[_] : ConcurrentEffect : LogIO](tx: Transactor[F], conf: Conf): Repo[F] = new Repo[F] {
    override def get(id: Long): OptionT[F, api.auth.User] = {
      OptionT(selectSql(id).option.transact(tx))
    }

    override def create(user: api.auth.User): F[Unit] = {
      insertSql(user)
        .run
        .transact(tx)
        .void
    }
  }

  private def insertSql(user: api.auth.User): Update0 =
    sql"""
       insert into sm_user(msisdn, first_name, last_name, role) values(
         ${user.msisdn},
         ${user.firstName},
         ${user.lastName},
         ${user.role}
       )
    """
      .stripMargin
      .update

  private def selectSql(userId: Long): Query0[api.auth.User] =
    sql"select msisdn, first_name, last_name, role from sm_user where id = $userId"
      .query[api.auth.User]
}