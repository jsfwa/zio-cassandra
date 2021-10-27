package zio.cassandra.cql.query

import com.datastax.oss.driver.api.core.cql.BoundStatement
import shapeless.HList
import zio.Task
import zio.cassandra.cql.{Binder, Reads}
import zio.cassandra.service.CassandraSession

case class QueryTemplate[V <: HList: Binder, R: Reads] private[cql] (
  query: String,
  config: BoundStatement => BoundStatement
) {
  def +(that: String): QueryTemplate[V, R] = QueryTemplate[V, R](this.query + that, config)
  def as[R1: Reads]: QueryTemplate[V, R1]  = QueryTemplate[V, R1](query, config)
  def prepare(session: CassandraSession): Task[PreparedQuery[V, R]] =
    session.prepare(query).map(new PreparedQuery(session, _, config))
  def config(config: BoundStatement => BoundStatement): QueryTemplate[V, R] =
    QueryTemplate[V, R](this.query, this.config andThen config)
  def stripMargin: QueryTemplate[V, R] = QueryTemplate[V, R](this.query.stripMargin, this.config)
}
