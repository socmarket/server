package socmarket.twoc.api.auth

import cats.Eq
import cats.instances.string._
import tsec.authorization.{AuthGroup, SimpleAuthEnum}

final case class Role(roleRepr: String)

object Role extends SimpleAuthEnum[Role, String] {

  val owner  : Role = Role("admin")
  val manager: Role = Role("manager")
  val cashier: Role = Role("cashier")

  override val values: AuthGroup[Role] = AuthGroup(owner, manager, cashier)

  override def getRepr(t: Role): String = t.roleRepr

  implicit val eqRole: Eq[Role] = Eq.fromUniversalEquals[Role]
}
