package socmarket.twoc.db.migration.steps

import doobie.ConnectionIO

object m20200806Init extends Step {
  def run(): ConnectionIO[Unit] = {
    execBatchFromResource("20200806A.sql")
  }
}