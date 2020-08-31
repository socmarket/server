package socmarket.twoc.api.auth

import cats.Applicative
import tsec.authorization.AuthorizationInfo

case class User(
  msisdn: Long,
  firstName: String,
  lastName: String,
  role: Role,
)

object User {
  implicit def authRole[F[_]](implicit F: Applicative[F]): AuthorizationInfo[F, Role, User] =
    (u: User) => F.pure(u.role)
}