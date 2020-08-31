package socmarket.twoc.db.repo

import java.time.Instant

import cats._
import cats.data._
import cats.effect.{Bracket, ConcurrentEffect, Resource}
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.implicits.legacy.instant._
import logstage.LogIO
import socmarket.twoc.config.Conf
import tsec.authentication.{AugmentedJWT, BackingStore}
import tsec.common.SecureRandomId
import tsec.jws.JWSSerializer
import tsec.jws.mac.{JWSMacCV, JWSMacHeader, JWTMacImpure}
import tsec.mac.jca.{MacErrorM, MacSigningKey}


object Auth {

  trait Repo[F[_], A] extends BackingStore[F, SecureRandomId, AugmentedJWT[A, Long]] {
    def put(jwt: AugmentedJWT[A, Long]): F[AugmentedJWT[A, Long]]
    def update(jwt: AugmentedJWT[A, Long]): F[AugmentedJWT[A, Long]]
    def delete(id: SecureRandomId): F[Unit]
    def get(id: SecureRandomId): OptionT[F, AugmentedJWT[A, Long]]
  }

  def createRepo[F[_]: ConcurrentEffect: LogIO, A](
    tx: Transactor[F],
    key: MacSigningKey[A],
    conf: Conf,
  )(
    implicit
    hs: JWSSerializer[JWSMacHeader[A]],
    s: JWSMacCV[MacErrorM, A],
  ): Resource[F, Repo[F, A]] = {
    Resource.make(ConcurrentEffect[F].delay(create(tx, key, conf)))(_ => ConcurrentEffect[F].delay(()))
  }

  private
  def create[F[_]: ConcurrentEffect: LogIO, A](
    tx: Transactor[F],
    key: MacSigningKey[A],
    conf: Conf,
  )(
    implicit
    hs: JWSSerializer[JWSMacHeader[A]],
    s: JWSMacCV[MacErrorM, A],
  ): Repo[F, A] = new Repo[F, A] {

    override def put(jwt: AugmentedJWT[A, Long]): F[AugmentedJWT[A, Long]] =
      insertSql(jwt).run.transact(tx).as(jwt)

    override def update(jwt: AugmentedJWT[A, Long]): F[AugmentedJWT[A, Long]] =
      updateSql(jwt).run.transact(tx).as(jwt)

    override def delete(id: SecureRandomId): F[Unit] =
      deleteSql(id).run.transact(tx).void

    override def get(id: SecureRandomId): OptionT[F, AugmentedJWT[A, Long]] =
      OptionT(selectSql(id).option.transact(tx)).semiflatMap {
        case (jwtStringify, identity, expiry, lastTouched) =>
          JWTMacImpure.verifyAndParse(jwtStringify, key) match {
            case Left(err) => err.raiseError[F, AugmentedJWT[A, Long]]
            case Right(jwt) => AugmentedJWT(id, jwt, identity, expiry, lastTouched).pure[F]
          }
      }

    private implicit val secureRandomIdPut: Put[SecureRandomId] =
      Put[String].contramap((_: Id[SecureRandomId]).widen)

    private def insertSql[A](jwt: AugmentedJWT[A, Long])(implicit hs: JWSSerializer[JWSMacHeader[A]]): Update0 =
      sql"""INSERT INTO JWT (ID, JWT, IDENTITY, EXPIRY, LAST_TOUCHED)
          VALUES (${jwt.id}, ${jwt.jwt.toEncodedString}, ${jwt.identity}, ${jwt.expiry}, ${jwt.lastTouched})
       """.update

    private def updateSql[A](jwt: AugmentedJWT[A, Long])(implicit hs: JWSSerializer[JWSMacHeader[A]]): Update0 =
      sql"""UPDATE JWT SET JWT = ${jwt.jwt.toEncodedString}, IDENTITY = ${jwt.identity},
           | EXPIRY = ${jwt.expiry}, LAST_TOUCHED = ${jwt.lastTouched} WHERE ID = ${jwt.id}
       """.stripMargin.update

    private def deleteSql(id: SecureRandomId): Update0 =
      sql"DELETE FROM JWT WHERE ID = $id".update

    private def selectSql(id: SecureRandomId): Query0[(String, Long, Instant, Option[Instant])] =
      sql"SELECT JWT, IDENTITY, EXPIRY, LAST_TOUCHED FROM JWT WHERE ID = $id"
        .query[(String, Long, Instant, Option[Instant])]
  }

}