package socmarket.twoc.service

import socmarket.twoc.adt.auth.Account
import socmarket.twoc.api.sync._
import socmarket.twoc.db.{repo => db}

import cats.effect.{ConcurrentEffect, Resource}
import cats.syntax.functor._
import cats.syntax.traverse._
import cats.instances.list._
import logstage.LogIO

object DataSync {

  trait Service[F[_]] {
    def syncProducts(account: Account, items: List[SyncProductReq]): F[Unit]
    def syncUnits(account: Account, items: List[SyncUnitReq]): F[Unit]
    def syncCurrencies(account: Account, items: List[SyncCurrencyReq]): F[Unit]
    def syncCategories(account: Account, items: List[SyncCategoryReq]): F[Unit]
    def syncClient(account: Account, items: List[SyncClientReq]): F[Unit]
    def syncSupplier(account: Account, items: List[SyncSupplierReq]): F[Unit]
    def syncSetting(account: Account, items: List[SyncSettingReq]): F[Unit]
    def syncBarcode(account: Account, items: List[SyncBarcodeReq]): F[Unit]
    def syncPrice(account: Account, items: List[SyncPriceReq]): F[Unit]
    def syncSaleCheck(account: Account, items: List[SyncSaleCheckReq]): F[Unit]
    def syncSaleCheckItem(account: Account, items: List[SyncSaleCheckItemReq]): F[Unit]
    def syncConsignment(account: Account, items: List[SyncConsignmentReq]): F[Unit]
    def syncConsignmentItem(account: Account, items: List[SyncConsignmentItemReq]): F[Unit]
    def syncSaleCheckRet(account: Account, items: List[SyncSaleCheckRetReq]): F[Unit]
    def syncConsignmentRet(account: Account, items: List[SyncConsignmentRetReq]): F[Unit]
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

    def syncProducts(account: Account, items: List[SyncProductReq]): F[Unit] = {
      items
        .traverse(dataSyncRepo.syncProduct(account.id, _))
        .void
    }

    def syncUnits(account: Account, items: List[SyncUnitReq]): F[Unit] = {
      items
        .traverse(dataSyncRepo.syncUnit(account.id, _))
        .void
    }

    def syncCurrencies(account: Account, items: List[SyncCurrencyReq]): F[Unit] = {
      items
        .traverse(dataSyncRepo.syncCurrency(account.id, _))
        .void
    }

    def syncCategories(account: Account, items: List[SyncCategoryReq]): F[Unit] = {
      items
        .traverse(dataSyncRepo.syncCategory(account.id, _))
        .void
    }

    def syncClient(account: Account, items: List[SyncClientReq]): F[Unit] = {
      items.traverse(dataSyncRepo.syncClient(account.id, _)).void
    }

    def syncSupplier(account: Account, items: List[SyncSupplierReq]): F[Unit] = {
      items.traverse(dataSyncRepo.syncSupplier(account.id, _)).void
    }

    def syncSetting(account: Account, items: List[SyncSettingReq]): F[Unit] = {
      items.traverse(dataSyncRepo.syncSetting(account.id, _)).void
    }

    def syncBarcode(account: Account, items: List[SyncBarcodeReq]): F[Unit] = {
      items.traverse(dataSyncRepo.syncBarcode(account.id, _)).void
    }

    def syncPrice(account: Account, items: List[SyncPriceReq]): F[Unit] = {
      items.traverse(dataSyncRepo.syncPrice(account.id, _)).void
    }

    def syncSaleCheck(account: Account, items: List[SyncSaleCheckReq]): F[Unit] = {
      items.traverse(dataSyncRepo.syncSaleCheck(account.id, _)).void
    }

    def syncSaleCheckItem(account: Account, items: List[SyncSaleCheckItemReq]): F[Unit] = {
      items.traverse(dataSyncRepo.syncSaleCheckItem(account.id, _)).void
    }

    def syncConsignment(account: Account, items: List[SyncConsignmentReq]): F[Unit] = {
      items.traverse(dataSyncRepo.syncConsignment(account.id, _)).void
    }

    def syncConsignmentItem(account: Account, items: List[SyncConsignmentItemReq]): F[Unit] = {
      items.traverse(dataSyncRepo.syncConsignmentItem(account.id, _)).void
    }

    def syncSaleCheckRet(account: Account, items: List[SyncSaleCheckRetReq]): F[Unit] = {
      items.traverse(dataSyncRepo.syncSaleCheckRet(account.id, _)).void
    }

    def syncConsignmentRet(account: Account, items: List[SyncConsignmentRetReq]): F[Unit] = {
      items.traverse(dataSyncRepo.syncConsignmentRet(account.id, _)).void
    }
  }
}