package socmarket.twoc.db.migration.steps

import doobie.ConnectionIO

object m20200807CreateUtcNow extends Step {
  def run(): ConnectionIO[Unit] = {
    execBatchFromResource("20200807ACreateUtcNow.sql")
  }
}