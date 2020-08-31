package socmarket.twoc.db.migration.steps

import doobie.ConnectionIO

object m20200831CreateWorkTables extends Step {
  def run(): ConnectionIO[Unit] = {
    execBatchFromResource("20200831A.sql")
  }
}