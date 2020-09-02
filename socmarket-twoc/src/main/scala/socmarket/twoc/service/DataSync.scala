package socmarket.twoc.service

import cats.effect.{ConcurrentEffect, Resource}
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import logstage.LogIO
import logstage.LogIO.log
import socmarket.twoc.adt.auth.{Account, AuthCodeInfo, AuthCodeSendInfo, AuthToken}
import socmarket.twoc.api.ApiErrorLimitExceeded
import socmarket.twoc.api.auth.AuthCodeVerifyReq
import socmarket.twoc.api.sync.SyncProductReq
import socmarket.twoc.db.{repo => db}
import socmarket.twoc.ext.{Nexmo, SmsPro}

object DataSync {

  trait Service[F[_]] {
    def syncProduct(account: Account, req: SyncProductReq): F[Unit]
  }

  def createService[F[_]: ConcurrentEffect: LogIO](
    dataSyncRepo: db.DataSync.Repo[F],
  ): Resource[F, Service[F]] = {
    Resource.make(
      ConcurrentEffect[F].delay(create(dataSyncRepo))
    )(
      _ => ConcurrentEffect[F].delay(())
    )
  }

  private def create[F[_]: ConcurrentEffect : LogIO](
    dataSyncRepo: db.DataSync.Repo[F],
  ): Service[F] = new Service[F] {

    private val F = implicitly[ConcurrentEffect[F]]

    def syncProduct(account: Account, req: SyncProductReq): F[Unit] = {
      dataSyncRepo.syncProduct(account.id, req)
    }
  }
}