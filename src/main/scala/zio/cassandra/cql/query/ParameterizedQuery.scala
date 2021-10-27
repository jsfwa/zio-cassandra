package zio.cassandra.cql.query

import com.datastax.oss.driver.api.core.cql.BoundStatement
import shapeless.HList
import zio.Task
import zio.cassandra.cql.{Binder, Reads}
import zio.cassandra.service.CassandraSession
import zio.stream.Stream

case class ParameterizedQuery[V <: HList: Binder, R: Reads] private (template: QueryTemplate[V, R], values: V) {
  def +(that: String): ParameterizedQuery[V, R] = ParameterizedQuery[V, R](this.template + that, this.values)
  def as[R1: Reads]: ParameterizedQuery[V, R1]  = ParameterizedQuery[V, R1](template.as[R1], values)
  def select(session: CassandraSession): Stream[Throwable, R] =
    Stream.unwrap(template.prepare(session).map(_.applyProduct(values).select))

  def selectOne(session: CassandraSession): Task[Option[R]] =
    template.prepare(session).flatMap(_.applyProduct(values).selectOne)
  def execute(session: CassandraSession): Task[Boolean] =
    template.prepare(session).map(_.applyProduct(values)).flatMap(_.execute)
  def config(config: BoundStatement => BoundStatement): ParameterizedQuery[V, R] =
    ParameterizedQuery[V, R](template.config(config), values)
  def stripMargin: ParameterizedQuery[V, R] = ParameterizedQuery[V, R](this.template.stripMargin, values)
}
