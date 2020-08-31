package socmarket.twoc.db.repo

import socmarket.twoc.api
import socmarket.twoc.config.Conf

import cats.syntax.functor._
import cats.effect.{ConcurrentEffect, Resource}
import doobie._
import doobie.implicits._
import logstage.LogIO

object Account {

  trait Repo[F[_]] {
    def get(accountId: Int): F[api.auth.Account]
    def create(account: api.auth.Account): F[Unit]
  }

  def createRepo[F[_] : ConcurrentEffect : LogIO](tx: Transactor[F], conf: Conf): Resource[F, Repo[F]] =
    Resource.make(ConcurrentEffect[F].delay(create(tx, conf)))(_ => ConcurrentEffect[F].delay(()))

  private
  def create[F[_] : ConcurrentEffect : LogIO](tx: Transactor[F], conf: Conf): Repo[F] = new Repo[F] {

    override def get(accountId: Int): F[api.auth.Account] = {
      selectSql(accountId)
        .unique
        .transact(tx)
    }

    override def create(account: api.auth.Account): F[Unit] = {
      insertSql(account)
        .run
        .transact(tx)
        .void
    }
  }

  private def insertSql(account: api.auth.Account): Update0 =
    sql"""
       insert into account(msisdn) values(
         ${account.msisdn}
       ) on conflict do nothing
    """
      .update

  private def selectSql(accountId: Long): Query0[api.auth.Account] =
    sql"select msisdn from account where id = $accountId"
      .query[api.auth.Account]
}