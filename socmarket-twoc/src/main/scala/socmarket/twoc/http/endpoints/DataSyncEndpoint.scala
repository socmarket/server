package socmarket.twoc.http.endpoints

import socmarket.twoc.adt.auth.Account
import socmarket.twoc.api.sync._
import socmarket.twoc.service.DataSync

import cats.effect.Sync
import cats.syntax.functor._
import cats.syntax.flatMap._
import org.http4s.{AuthedRoutes, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware

object DataSyncEndpoint {

  import org.http4s.circe.CirceEntityDecoder._

  def router[F[_]: Sync](
    dsync: DataSync.Service[F],
    authM: AuthMiddleware[F, Account],
  ): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = Http4sDsl[F]
    import dsl._
    authM(
      AuthedRoutes.of[Account, F] {
        case req @ POST -> Root / "product" as acc =>
          for {
            syncReq <- req.req.as[List[SyncProductReq]]
            _ <- dsync.syncProducts(acc, syncReq)
            res <- Ok("OK")
          } yield res
        case req @ POST -> Root / "unit" as acc =>
          for {
            syncReq <- req.req.as[List[SyncUnitReq]]
            _ <- dsync.syncUnits(acc, syncReq)
            res <- Ok("OK")
          } yield res
        case req @ POST -> Root / "currency" as acc =>
          for {
            syncReq <- req.req.as[List[SyncCurrencyReq]]
            _ <- dsync.syncCurrencies(acc, syncReq)
            res <- Ok("OK")
          } yield res
        case req @ POST -> Root / "category" as acc =>
          for {
            syncReq <- req.req.as[List[SyncCategoryReq]]
            _ <- dsync.syncCategories(acc, syncReq)
            res <- Ok("OK")
          } yield res
        case req @ POST -> Root / "client" as acc =>
          for {
            syncReq <- req.req.as[List[SyncClientReq]]
            _ <- dsync.syncClient(acc, syncReq)
            res <- Ok("OK")
          } yield res
        case req @ POST -> Root / "supplier" as acc =>
          for {
            syncReq <- req.req.as[List[SyncSupplierReq]]
            _ <- dsync.syncSupplier(acc, syncReq)
            res <- Ok("OK")
          } yield res
        case req @ POST -> Root / "setting" as acc =>
          for {
            syncReq <- req.req.as[List[SyncSettingReq]]
            _ <- dsync.syncSetting(acc, syncReq)
            res <- Ok("OK")
          } yield res
        case req @ POST -> Root / "barcode" as acc =>
          for {
            syncReq <- req.req.as[List[SyncBarcodeReq]]
            _ <- dsync.syncBarcode(acc, syncReq)
            res <- Ok("OK")
          } yield res
        case req @ POST -> Root / "price" as acc =>
          for {
            syncReq <- req.req.as[List[SyncPriceReq]]
            _ <- dsync.syncPrice(acc, syncReq)
            res <- Ok("OK")
          } yield res
        case req @ POST -> Root / "salecheck" as acc =>
          for {
            syncReq <- req.req.as[List[SyncSaleCheckReq]]
            _ <- dsync.syncSaleCheck(acc, syncReq)
            res <- Ok("OK")
          } yield res
        case req @ POST -> Root / "salecheckitem" as acc =>
          for {
            syncReq <- req.req.as[List[SyncSaleCheckItemReq]]
            _ <- dsync.syncSaleCheckItem(acc, syncReq)
            res <- Ok("OK")
          } yield res
        case req @ POST -> Root / "consignment" as acc =>
          for {
            syncReq <- req.req.as[List[SyncConsignmentReq]]
            _ <- dsync.syncConsignment(acc, syncReq)
            res <- Ok("OK")
          } yield res
        case req @ POST -> Root / "consignmentitem" as acc =>
          for {
            syncReq <- req.req.as[List[SyncConsignmentItemReq]]
            _ <- dsync.syncConsignmentItem(acc, syncReq)
            res <- Ok("OK")
          } yield res
        case req @ POST -> Root / "salecheckret" as acc =>
          for {
            syncReq <- req.req.as[List[SyncSaleCheckRetReq]]
            _ <- dsync.syncSaleCheckRet(acc, syncReq)
            res <- Ok("OK")
          } yield res
        case req @ POST -> Root / "consignmentret" as acc =>
          for {
            syncReq <- req.req.as[List[SyncConsignmentRetReq]]
            _ <- dsync.syncConsignmentRet(acc, syncReq)
            res <- Ok("OK")
          } yield res
      }
    )
  }

}