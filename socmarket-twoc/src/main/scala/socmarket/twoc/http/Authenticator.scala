package socmarket.twoc.http

import cats.MonadError
import cats.effect._
import org.http4s.Response
import socmarket.twoc.api.auth.{Role, User}
import tsec.authentication.{AugmentedJWT, BackingStore, IdentityStore, JWTAuthenticator, SecuredRequest, TSecAuthService}
import tsec.authorization.BasicRBAC
import tsec.common.SecureRandomId
import tsec.jws.mac.JWSMacCV
import tsec.jwt.algorithms.JWTMacAlgo
import tsec.mac.jca.MacSigningKey

import scala.concurrent.duration._

object Authenticator {

  def jwt[F[_] : Sync, Auth: JWTMacAlgo](
    key: MacSigningKey[Auth],
    authRepo: BackingStore[F, SecureRandomId, AugmentedJWT[Auth, Long]],
    userRepo: IdentityStore[F, Long, User],
  )(implicit cv: JWSMacCV[F, Auth]): JWTAuthenticator[F, Long, User, Auth] =
    JWTAuthenticator.backed.inBearerToken(
      expiryDuration = 1.hour,
      maxIdle = None,
      tokenStore = authRepo,
      identityStore = userRepo,
      signingKey = key,
    )

  private def _allRoles[F[_], Auth](implicit F: MonadError[F, Throwable]) =
    BasicRBAC.all[F, Role, User, Auth]

  def allRoles[F[_], Auth](
    pf: PartialFunction[SecuredRequest[F, User, AugmentedJWT[Auth, Long]], F[Response[F]]],
  )(implicit F: MonadError[F, Throwable]): TSecAuthService[User, AugmentedJWT[Auth, Long], F] =
    TSecAuthService.withAuthorization(_allRoles[F, AugmentedJWT[Auth, Long]])(pf)

  def allRolesHandler[F[_], Auth](
    pf: PartialFunction[SecuredRequest[F, User, AugmentedJWT[Auth, Long]], F[Response[F]]],
  )(
    onNotAuthorized: TSecAuthService[User, AugmentedJWT[Auth, Long], F]
  )(implicit F: MonadError[F, Throwable]): TSecAuthService[User, AugmentedJWT[Auth, Long], F] =
    TSecAuthService.withAuthorizationHandler(_allRoles[F, AugmentedJWT[Auth, Long]])(
      pf,
      onNotAuthorized.run,
    )

  private def _owner[F[_], Auth](implicit F: MonadError[F, Throwable]) =
    BasicRBAC[F, Role, User, Auth](Role.owner)

  def ownerOnly[F[_], Auth](
    pf: PartialFunction[SecuredRequest[F, User, AugmentedJWT[Auth, Long]], F[Response[F]]],
  )(implicit F: MonadError[F, Throwable]): TSecAuthService[User, AugmentedJWT[Auth, Long], F] =
    TSecAuthService.withAuthorization(_owner[F, AugmentedJWT[Auth, Long]])(pf)

}