package socmarket.twoc.db.repo

import socmarket.twoc.api.sync._
import socmarket.twoc.config.Conf

import cats.effect.{ConcurrentEffect, Resource}
import cats.syntax.functor._
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.{Transactor, Update0}
import logstage.LogIO

object DataSync {

  trait Repo[F[_]] {
    def syncProduct(accountId: Int, req: SyncProductReq): F[Unit]
    def syncUnit(accountId: Int, req: SyncUnitReq): F[Unit]
    def syncCurrency(accountId: Int, req: SyncCurrencyReq): F[Unit]
    def syncCategory(accountId: Int, req: SyncCategoryReq): F[Unit]
    def syncClient(accountId: Int, req: SyncClientReq): F[Unit]
    def syncSupplier(accountId: Int, req: SyncSupplierReq): F[Unit]
    def syncSetting(accountId: Int, req: SyncSettingReq): F[Unit]
    def syncBarcode(accountId: Int, req: SyncBarcodeReq): F[Unit]
    def syncPrice(accountId: Int, req: SyncPriceReq): F[Unit]
    def syncSaleCheck(accountId: Int, req: SyncSaleCheckReq): F[Unit]
    def syncSaleCheckItem(accountId: Int, req: SyncSaleCheckItemReq): F[Unit]
    def syncConsignment(accountId: Int, req: SyncConsignmentReq): F[Unit]
    def syncConsignmentItem(accountId: Int, req: SyncConsignmentItemReq): F[Unit]
    def syncSaleCheckRet(accountId: Int, req: SyncSaleCheckRetReq): F[Unit]
    def syncConsignmentRet(accountId: Int, req: SyncConsignmentRetReq): F[Unit]
  }

  def createRepo[F[_]: ConcurrentEffect: LogIO](tx: Transactor[F], conf: Conf): Resource[F, Repo[F]] =
    Resource.make(ConcurrentEffect[F].delay(create(tx, conf)))(_ => ConcurrentEffect[F].delay(()))

  private
  def create[F[_]: ConcurrentEffect: LogIO](tx: Transactor[F], conf: Conf): Repo[F] = new Repo[F] {

    private val F: ConcurrentEffect[F] = implicitly[ConcurrentEffect[F]]

    def syncProduct(accountId: Int, req: SyncProductReq): F[Unit] = {
      upsertProductSql(accountId, req).run.transact(tx).void
    }

    def syncUnit(accountId: Int, req: SyncUnitReq): F[Unit] = {
      upsertUnitSql(accountId, req).run.transact(tx).void
    }

    def syncCurrency(accountId: Int, req: SyncCurrencyReq): F[Unit] = {
      upsertCurrencySql(accountId, req).run.transact(tx).void
    }

    def syncCategory(accountId: Int, req: SyncCategoryReq): F[Unit] = {
      upsertCategorySql(accountId, req).run.transact(tx).void
    }

    def syncClient(accountId: Int, req: SyncClientReq): F[Unit] = {
      upsertClientSql(accountId, req).run.transact(tx).void
    }

    def syncSupplier(accountId: Int, req: SyncSupplierReq): F[Unit] = {
      upsertSupplierSql(accountId, req).run.transact(tx).void
    }

    def syncSetting(accountId: Int, req: SyncSettingReq): F[Unit] = {
      upsertSettingSql(accountId, req).run.transact(tx).void
    }

    def syncBarcode(accountId: Int, req: SyncBarcodeReq): F[Unit] = {
      upsertBarcodeSql(accountId, req).run.transact(tx).void
    }

    def syncPrice(accountId: Int, req: SyncPriceReq): F[Unit] = {
      upsertPriceSql(accountId, req).run.transact(tx).void
    }

    def syncSaleCheck(accountId: Int, req: SyncSaleCheckReq): F[Unit] = {
      upsertSaleCheckSql(accountId, req).run.transact(tx).void
    }

    def syncSaleCheckItem(accountId: Int, req: SyncSaleCheckItemReq): F[Unit] = {
      upsertSaleCheckItemSql(accountId, req).run.transact(tx).void
    }

    def syncConsignment(accountId: Int, req: SyncConsignmentReq): F[Unit] = {
      upsertConsignmentSql(accountId, req).run.transact(tx).void
    }

    def syncConsignmentItem(accountId: Int, req: SyncConsignmentItemReq): F[Unit] = {
      upsertConsignmentItemSql(accountId, req).run.transact(tx).void
    }

    def syncSaleCheckRet(accountId: Int, req: SyncSaleCheckRetReq): F[Unit] = {
      upsertSaleCheckRetSql(accountId, req).run.transact(tx).void
    }

    def syncConsignmentRet(accountId: Int, req: SyncConsignmentRetReq): F[Unit] = {
      upsertConsignmentRetSql(accountId, req).run.transact(tx).void
    }
  }

  private
  def upsertProductSql(accountId: Int, req: SyncProductReq): Update0 = fr"""
    insert into product(
      account_id,
      id,
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
      coord
    ) values (
      $accountId,
      ${req.id},
      ${req.barcode},
      ${req.code},
      ${req.title},
      ${req.notes},
      ${req.unitId},
      ${req.categoryId},
      ${req.brand},
      ${req.model},
      ${req.engine},
      ${req.oemNo},
      ${req.serial},
      ${req.coord}
    ) on conflict (account_id, id) do update set
      barcode     = ${req.barcode},
      code        = ${req.code},
      title       = ${req.title},
      notes       = ${req.notes},
      unit_id     = ${req.unitId},
      category_id = ${req.categoryId},
      brand       = ${req.brand},
      model       = ${req.model},
      engine      = ${req.engine},
      oemno       = ${req.oemNo},
      serial      = ${req.serial},
      coord       = ${req.coord},
      updated_at  = utcnow()
  """.update

  private
  def upsertUnitSql(accountId: Int, req: SyncUnitReq): Update0 = fr"""
    insert into unit(
      account_id,
      id,
      title,
      notation
    ) values (
      $accountId,
      ${req.id},
      ${req.title},
      ${req.notation}
    ) on conflict (account_id, id) do update set
      title       = ${req.title},
      notation    = ${req.notation},
      updated_at  = utcnow()
  """.update

  private
  def upsertCurrencySql(accountId: Int, req: SyncCurrencyReq): Update0 = fr"""
    insert into currency(
      account_id,
      id,
      title,
      notation
    ) values (
      $accountId,
      ${req.id},
      ${req.title},
      ${req.notation}
    ) on conflict (account_id, id) do update set
      title       = ${req.title},
      notation    = ${req.notation},
      updated_at  = utcnow()
  """.update

  private
  def upsertCategorySql(accountId: Int, req: SyncCategoryReq): Update0 = fr"""
    insert into category(
      account_id,
      id,
      parent_id,
      title,
      notes
    ) values (
      $accountId,
      ${req.id},
      ${req.parentId},
      ${req.title},
      ${req.notes}
    ) on conflict (account_id, id) do update set
      parent_id  = ${req.parentId},
      title      = ${req.title},
      notes     = ${req.notes},
      updated_at = utcnow()
  """.update

  private
  def upsertClientSql(accountId: Int, req: SyncClientReq): Update0 = fr"""
    insert into client(
      account_id,
      id,
      name,
      contacts,
      notes
    ) values (
      $accountId,
      ${req.id},
      ${req.name},
      ${req.contacts},
      ${req.notes}
    ) on conflict (account_id, id) do update set
      name       = ${req.name},
      contacts   = ${req.contacts},
      notes      = ${req.notes},
      updated_at = utcnow()
  """.update

  private
  def upsertSupplierSql(accountId: Int, req: SyncSupplierReq): Update0 = fr"""
    insert into supplier(
      account_id,
      id,
      name,
      contacts,
      notes
    ) values (
      $accountId,
      ${req.id},
      ${req.name},
      ${req.contacts},
      ${req.notes}
    ) on conflict (account_id, id) do update set
      name       = ${req.name},
      contacts   = ${req.contacts},
      notes      = ${req.notes},
      updated_at = utcnow()
  """.update

  private
  def upsertSettingSql(accountId: Int, req: SyncSettingReq): Update0 = fr"""
    insert into settings(
      account_id,
      id,
      key,
      value
    ) values (
      $accountId,
      ${req.id},
      ${req.key},
      ${req.value}
    ) on conflict (account_id, id) do update set
      key        = ${req.key},
      value      = ${req.value},
      updated_at = utcnow()
  """.update

  private
  def upsertBarcodeSql(accountId: Int, req: SyncBarcodeReq): Update0 = fr"""
    insert into barcode(
      account_id,
      code
    ) values (
      $accountId,
      ${req.code}
    ) on conflict (account_id, code) do update set
      code       = ${req.code},
      updated_at = utcnow()
  """.update

  private
  def upsertPriceSql(accountId: Int, req: SyncPriceReq): Update0 = fr"""
    insert into price(
      account_id,
      id,
      product_id,
      currency_id,
      price,
      set_at
    ) values (
      $accountId,
      ${req.id},
      ${req.productId},
      ${req.currencyId},
      ${req.price},
      ${req.setAt}
    ) on conflict (account_id, id) do update set
      product_id  = ${req.productId},
      currency_id = ${req.currencyId},
      price       = ${req.price},
      set_at      = ${req.setAt},
      updated_at = utcnow()
  """.update

  private
  def upsertSaleCheckSql(accountId: Int, req: SyncSaleCheckReq): Update0 = fr"""
    insert into salecheck(
      account_id,
      id,
      client_id,
      cash,
      change,
      discount,
      closed,
      sold_at
    ) values (
      $accountId,
      ${req.id},
      ${req.clientId},
      ${req.cash},
      ${req.change},
      ${req.discount},
      ${req.closed} :: boolean,
      ${req.soldAt}
    ) on conflict (account_id, id) do update set
      client_id   = ${req.clientId},
      cash        = ${req.cash},
      change      = ${req.change},
      discount    = ${req.discount},
      closed      = ${req.closed} :: boolean,
      sold_at     = ${req.soldAt},
      updated_at = utcnow()
  """.update

  private
  def upsertSaleCheckItemSql(accountId: Int, req: SyncSaleCheckItemReq): Update0 = fr"""
    insert into salecheckitem(
      account_id,
      id,
      sale_check_id,
      product_id,
      unit_id,
      currency_id,
      quantity,
      original_price,
      price,
      discount
    ) values (
      $accountId,
      ${req.id},
      ${req.saleCheckId},
      ${req.productId},
      ${req.unitId},
      ${req.currencyId},
      ${req.quantity},
      ${req.originalPrice},
      ${req.price},
      ${req.discount}
    ) on conflict (account_id, id) do update set
      sale_check_id  = ${req.saleCheckId},
      product_id     = ${req.productId},
      unit_id        = ${req.unitId},
      currency_id    = ${req.currencyId},
      quantity       = ${req.quantity},
      original_price = ${req.originalPrice},
      price          = ${req.price},
      discount       = ${req.discount},
      updated_at     = utcnow()
  """.update

  private
  def upsertConsignmentSql(accountId: Int, req: SyncConsignmentReq): Update0 = fr"""
    insert into consignment(
      account_id,
      id,
      supplier_id,
      closed,
      accepted_at
    ) values (
      $accountId,
      ${req.id},
      ${req.supplierId},
      ${req.closed} :: boolean,
      ${req.acceptedAt}
    ) on conflict (account_id, id) do update set
      supplier_id = ${req.supplierId},
      closed      = ${req.closed} :: boolean,
      accepted_at = ${req.acceptedAt},
      updated_at  = utcnow()
  """.update

  private
  def upsertConsignmentItemSql(accountId: Int, req: SyncConsignmentItemReq): Update0 = fr"""
    insert into consignmentitem(
      account_id,
      id,
      consignment_id,
      product_id,
      unit_id,
      currency_id,
      quantity,
      price
    ) values (
      $accountId,
      ${req.id},
      ${req.consignmentId},
      ${req.productId},
      ${req.unitId},
      ${req.currencyId},
      ${req.quantity},
      ${req.price}
    ) on conflict (account_id, id) do update set
      consignment_id = ${req.consignmentId},
      product_id     = ${req.productId},
      unit_id        = ${req.unitId},
      currency_id    = ${req.currencyId},
      quantity       = ${req.quantity},
      price          = ${req.price},
      updated_at     = utcnow()
  """.update

  private
  def upsertSaleCheckRetSql(accountId: Int, req: SyncSaleCheckRetReq): Update0 = fr"""
    insert into salecheckret(
      account_id,
      sale_check_item_id,
      quantity,
      notes,
      returned_at
    ) values (
      $accountId,
      ${req.saleCheckItemId},
      ${req.quantity},
      ${req.notes},
      ${req.returnedAt}
    ) on conflict (account_id, sale_check_item_id) do update set
      sale_check_item_id = ${req.saleCheckItemId},
      quantity           = ${req.quantity},
      notes              = ${req.notes},
      returned_at        = ${req.returnedAt},
      updated_at = utcnow()
  """.update

  private
  def upsertConsignmentRetSql(accountId: Int, req: SyncConsignmentRetReq): Update0 = fr"""
    insert into consignmentret(
      account_id,
      consignment_item_id,
      quantity,
      notes,
      returned_at
    ) values (
      $accountId,
      ${req.consignmentItemId},
      ${req.quantity},
      ${req.notes},
      ${req.returnedAt}
    ) on conflict (account_id, consignment_item_id) do update set
      sale_check_item_id = ${req.consignmentItemId},
      quantity           = ${req.quantity},
      notes              = ${req.notes},
      returned_at        = ${req.returnedAt},
      updated_at = utcnow()
  """.update

}