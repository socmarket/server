package socmarket.twoc.environment.db.migration.steps

import zio.Task

object m20200725Init extends Step {
  def run(meta: Meta): Task[Unit] = {
    execBatchFromResource("20200725Init.sql", meta)
  }
}