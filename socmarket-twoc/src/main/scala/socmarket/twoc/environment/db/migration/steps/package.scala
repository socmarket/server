package socmarket.twoc.environment.db.migration

import doobie.util.fragment.Fragment
import socmarket.twoc.environment.db.Database
import zio.{Task, ZIO}

package object steps {

  trait Step {
    def key: String = getClass.getSimpleName
    def run(meta: Meta): Task[Unit]
  }

  case class Meta(
    db: Database.Service,
    offsetDt: java.time.OffsetDateTime,
  )

  def sqlQuery(sql: String): Fragment = Fragment.const(sql)

  def execBatchFromResource(name: String, meta: Meta): Task[Unit] = {
    readStep(name).flatMap(sql => meta.db.exec(sql))
  }

  def readStep(name: String): Task[String] = {
    val path = "/socmarket/twoc/environment/migration/steps/"
    ZIO.effect(Option(getClass.getResourceAsStream(path + name)))
      .someOrFail(new RuntimeException("Can't find migration file: " + name))
      .flatMap(is => ZIO.effect(scala.io.Source.fromInputStream(is)))
      .map(_.getLines().mkString("\n"))
  }

  def list(): List[Step] = {
    List[Step](
      steps.m20200725Init,
      steps.m20200725Init,
    )
  }
}