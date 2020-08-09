package socmarket.twoc.db.migration

import cats.effect.{IO, LiftIO}
import doobie.implicits._
import doobie.ConnectionIO
import doobie.util.fragment.Fragment

package object steps {

  trait Step {
    def key: String = getClass.getSimpleName
    def run(): ConnectionIO[Unit]
  }

  def diff(lastKey: String): List[ConnectionIO[Unit]] = {
    List[Step](
      steps.m20200806Init,
      steps.m20200807CreateUtcNow,
    ).filter(_.key > lastKey).map(mkStep)
  }

  def execBatchFromResource[F[_]](name: String): ConnectionIO[Unit] = {
    for {
      sql <- LiftIO[ConnectionIO].liftIO(readStep(name))
      _   <- Fragment.const(sql).update.run
    } yield ()
  }

  private def mkStep(step: Step): ConnectionIO[Unit] = {
    for {
      _     <- step.run()
      _     <- fr"insert into migration(mkey) values(${step.key})".update.run
    } yield ()
  }

  private def readStep(name: String): IO[String] = {
    val path = "/socmarket/twoc/environment/migration/steps/"
    IO(Option(getClass.getResourceAsStream(path + name))
      .getOrElse(throw new RuntimeException("Can't find migration file: " + name))
    )
      .flatMap(is => IO(scala.io.Source.fromInputStream(is)))
      .map(_.getLines().mkString("\n"))
  }
}