package zio.cassandra.cql.query

import com.datastax.oss.driver.api.core.cql.BoundStatement
import zio.Task
import zio.cassandra.cql.Reads
import zio.cassandra.service.CassandraSession
import zio.stream.Stream

class Query[R: Reads] private[cql] (
  session: CassandraSession,
  private[cql] val statement: BoundStatement
) {
  def config(statement: BoundStatement => BoundStatement) = new Query[R](session, statement(this.statement))
  def select: Stream[Throwable, R]                        = session.select(statement).map(Reads[R].read(_, 0)._1)
  def selectOne: Task[Option[R]]                          = session.selectOne(statement).map(_.map(row => Reads[R].read(row, 0)._1))
  def execute: Task[Boolean]                              = session.execute(statement).map(_.wasApplied)
}
