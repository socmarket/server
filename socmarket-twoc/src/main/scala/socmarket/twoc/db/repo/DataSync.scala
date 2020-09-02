package socmarket.twoc.db.repo

import socmarket.twoc.api.sync.SyncProductReq
import socmarket.twoc.config.Conf

import cats.effect.{ConcurrentEffect, Resource}
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import doobie.implicits._
import doobie.{Transactor, Update0}
import logstage.LogIO

object DataSync {

  trait Repo[F[_]] {
    def syncProduct(accountId: Int, req: SyncProductReq): F[Unit]
  }

  def createRepo[F[_]: ConcurrentEffect: LogIO](tx: Transactor[F], conf: Conf): Resource[F, Repo[F]] =
    Resource.make(ConcurrentEffect[F].delay(create(tx, conf)))(_ => ConcurrentEffect[F].delay(()))

  private
  def create[F[_]: ConcurrentEffect: LogIO](tx: Transactor[F], conf: Conf): Repo[F] = new Repo[F] {

    private val F: ConcurrentEffect[F] = implicitly[ConcurrentEffect[F]]

    def syncProduct(accountId: Int, req: SyncProductReq): F[Unit] = {
      upsertProductSql(accountId, req)
        .run
        .transact(tx)
        .void
    }

  }

  private
  def upsertProductSql(accountId: Int, req: SyncProductReq): Update0 = fr"""
    insert into product(
      account_id,
      product_id,
      barcode,
      code,
      title,
      notes,
      unit_id,
      category_id,
      brand,
      model,
      engine,
      oemno,
      serial,
      coord,
      updated_at
    ) values (
      $accountId,
      ${req.productId},
      ${req.barcode},
      ${req.code},
      ${req.title},
      ${req.notes},
      ${req.unitId},
      ${req.categoryId},
      ${req.brand},
      ${req.model},
      ${req.engine},
      ${req.oemno},
      ${req.serial},
      ${req.coord},
      utcnow()
    ) on conflict (accountId, productId) do update set
      barcode     = ${req.barcode},
      code        = ${req.code},
      title       = ${req.title},
      notes       = ${req.notes},
      unit_id     = ${req.unitId},
      category_id = ${req.categoryId},
      brand       = ${req.brand},
      model       = ${req.model},
      engine      = ${req.engine},
      oemno       = ${req.oemno},
      serial      = ${req.serial},
      coord       = ${req.coord},
      updated_at  = utcnow()
  """.update

}