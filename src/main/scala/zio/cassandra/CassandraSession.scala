package zio.cassandra

import com.datastax.oss.driver.api.core.cql._
import com.datastax.oss.driver.api.core.metrics.Metrics
import com.datastax.oss.driver.api.core.{ CqlSession, CqlSessionBuilder }
import com.datastax.oss.driver.internal.core.config.typesafe.DefaultDriverConfigLoader
import com.typesafe.config.Config
import zio.stream.ZStream.Pull
import zio.stream.{ Stream, ZStream }
import zio.{ Chunk, Ref, Task, TaskManaged, ZIO }

import java.net.InetSocketAddress
import java.util.concurrent.CompletionStage
import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters.RichOptional

trait CassandraSession {
  def prepare(stmt: String): Task[PreparedStatement]

  def bind(stmt: PreparedStatement, bindValues: Seq[AnyRef]): Task[BoundStatement]

  def execute(stmt: Statement[_]): Task[AsyncResultSet]

  def execute(query: String): Task[AsyncResultSet]

  def select(stmt: Statement[_]): Stream[Throwable, Row]

  // metrics
  def metrics: Option[Metrics]

  // short-cuts
  def bind(stmt: PreparedStatement, bindValues: Seq[AnyRef], profileName: String): Task[BoundStatement] =
    bind(stmt, bindValues).map(_.setExecutionProfileName(profileName))

  def bindAndExecute(stmt: PreparedStatement, bindValues: Seq[AnyRef], profileName: String): Task[AsyncResultSet] =
    for {
      bound <- bind(stmt, bindValues)
      res   <- execute(bound.setExecutionProfileName(profileName))
    } yield res

  def bindAndExecute(stmt: PreparedStatement, bindValues: Seq[AnyRef]): Task[AsyncResultSet] =
    for {
      bound <- bind(stmt, bindValues)
      res   <- execute(bound)
    } yield res

  def bindAndSelect(stmt: PreparedStatement, bindValues: Seq[AnyRef]): Stream[Throwable, Row] =
    ZStream.fromEffect(bind(stmt, bindValues)).flatMap(select)

  def bindAndSelect(
    stmt: PreparedStatement,
    bindValues: Seq[AnyRef],
    profileName: String
  ): Stream[Throwable, Row] =
    ZStream.fromEffect(bind(stmt, bindValues, profileName)).flatMap(select)

  def bindAndSelectAll(stmt: PreparedStatement, bindValues: Seq[AnyRef]): Task[Seq[Row]] =
    for {
      bound <- bind(stmt, bindValues)
      res   <- selectAll(bound)
    } yield res

  def bindAndSelectAll(stmt: PreparedStatement, bindValues: Seq[AnyRef], profileName: String): Task[Seq[Row]] =
    for {
      bound <- bind(stmt, bindValues, profileName)
      res   <- selectAll(bound)
    } yield res

  def executeBatch(seq: Seq[BoundStatement], batchType: DefaultBatchType): Task[AsyncResultSet] = {
    val batch = BatchStatement
      .builder(batchType)
      .addStatements(seq: _*)
      .build()
    execute(batch)
  }

  def selectOne(stmt: Statement[_]): Task[Option[Row]] =
    execute(stmt).map(rs => Option(rs.one()))

  def selectAll(stmt: Statement[_]): Task[Seq[Row]] =
    select(stmt).runCollect
}

object CassandraSession {

  import Task.{ fromCompletionStage => fromJavaAsync }

  class Live(underlying: CqlSession) extends CassandraSession {
    override def prepare(stmt: String): Task[PreparedStatement] =
      fromJavaAsync(underlying.prepareAsync(stmt))

    override def execute(stmt: Statement[_]): Task[AsyncResultSet] =
      fromJavaAsync(underlying.executeAsync(stmt))

    override def bind(stmt: PreparedStatement, bindValues: Seq[AnyRef]): Task[BoundStatement] =
      Task(stmt.bind(bindValues: _*))

    override def metrics: Option[Metrics] = underlying.getMetrics.toScala

    override def select(stmt: Statement[_]): Stream[Throwable, Row] = {
      def pull(ref: Ref[ZIO[Any, Option[Throwable], AsyncResultSet]]): ZIO[Any, Option[Throwable], Chunk[Row]] =
        for {
          io <- ref.get
          rs <- io
          _ <- rs match {
                case _ if rs.hasMorePages =>
                  ref.set(fromJavaAsync(rs.fetchNextPage()).mapError(Option(_)))
                case _ if rs.currentPage().iterator().hasNext => ref.set(Pull.end)
                case _                                        => Pull.end
              }
        } yield Chunk.fromArray(rs.currentPage().asScala.toArray)

      Stream {
        for {
          ref <- Ref.make(execute(stmt).mapError(Option(_))).toManaged_
        } yield pull(ref)
      }
    }

    override def execute(query: String): Task[AsyncResultSet] =
      fromJavaAsync(underlying.executeAsync(query))
  }

  def make(builder: CqlSessionBuilder): TaskManaged[CassandraSession] =
    make(builder.buildAsync())

  def make(config: Config): TaskManaged[CassandraSession] =
    make(
      CqlSession
        .builder()
        .withConfigLoader(new DefaultDriverConfigLoader(() => config, false))
    )

  def make(
    config: Config,
    contactPoints: Seq[InetSocketAddress],
    auth: Option[(String, String)] = None
  ): TaskManaged[CassandraSession] = {
    val builder = CqlSession
      .builder()
      .withConfigLoader(new DefaultDriverConfigLoader(() => config, false))
      .addContactPoints(contactPoints.asJavaCollection)

    make(auth.fold(builder) {
      case (username, password) =>
        builder.withAuthCredentials(username, password)
    })
  }

  private def make(session: => CompletionStage[CqlSession]): TaskManaged[CassandraSession] =
    fromJavaAsync(session).toManaged(session => fromJavaAsync(session.closeAsync()).orDie).map(new Live(_))

}
