# zio-cassandra

[![Release Artifacts][Badge-SonatypeReleases]][Link-SonatypeReleases] [![Snapshot Artifacts][Badge-SonatypeSnapshots]][Link-SonatypeSnapshots]

[Link-SonatypeReleases]: https://oss.sonatype.org/content/repositories/releases/io/github/jsfwa/zio-cassandra_2.13/ "Sonatype Releases"
[Link-SonatypeSnapshots]: https://oss.sonatype.org/content/repositories/snapshots/io/github/jsfwa/zio-cassandra_2.13/ "Sonatype Snapshots"
[Badge-SonatypeReleases]: https://img.shields.io/nexus/r/https/oss.sonatype.org/io.github.jsfwa/zio-cassandra_2.13.svg "Sonatype Releases"
[Badge-SonatypeSnapshots]: https://img.shields.io/nexus/s/https/oss.sonatype.org/io.github.jsfwa/zio-cassandra_2.13.svg "Sonatype Snapshots"

This is lightweight ZIO wrapper for latest datastax 4.x driver.


```text
cassandra-driver = 4.13.0
zio = 1.0.12
```

Inspired by [akka/alpakka-cassandra](https://doc.akka.io/docs/alpakka/current/cassandra.html)
CQL ported from [ringcentral/cassandra4io](https://github.com/ringcentral/cassandra4io)


## Usage

#### Dependency:
```scala
libraryDependencies += "io.github.jsfwa" %% "zio-cassandra" % "1.1.0"
```

### Create a connection to Cassandra
```scala
import zio.cassandra.CassandraSession

import com.datastax.oss.driver.api.core.CqlSession

import java.net.InetSocketAddress

val builder = CqlSession
      .builder()
      .addContactPoint(InetSocketAddress.createUnresolved("localhost", 9042))
      .withLocalDatacenter("datacenter1")
      .withKeyspace("awesome") 

val session = CassandraSession.make(builder)
```

## Work with CQL interpolator

Gently ported from [cassadnra4io](https://github.com/ringcentral/cassandra4io) cql 
package `zio.cassandra.cql` introduces typed way to deal with cql queries:

### Simple syntax

This syntax reuse implicit driver prepared statements cache

```scala
import com.datastax.oss.driver.api.core.ConsistencyLevel
import zio.cassandra.CassandraSession
import zio.cassandra.cql._
import zio._

case class Model(id: Int, data: String)

trait Service {
  def put(value: Model): Task[Unit]
  def get(id: Int): Task[Option[Model]]
}

class ServiceImpl(session: CassandraSession) extends Service {

  private def insertQuery(value: Model) =
    cql"insert into table (id, data) values (${value.id}, ${value.data})"
      .config(_.setConsistencyLevel(ConsistencyLevel.ALL))

  private def selectQuery(id: Int) =
    cql"select id, data from table where id = $id".as[Model]
  
  override def put(value: Model) = insertQuery(value).execute(session).unit
  override def get(id: Int) = selectQuery(id).selectOne(session)
}
```

### Templated syntax

When you want control your prepared statements manually.

```scala
import zio.duration._
import com.datastax.oss.driver.api.core.ConsistencyLevel
import zio.cassandra.CassandraSession
import zio.cassandra.cql._
import zio.stream._
    
case class Model(id: Int, data: String)
  
trait Service {
  def put(value: Model): Task[Unit]
  def get(id: Int): Task[Option[Model]]
  def getAll(): Stream[Throwable, Model]
}
    
object Dao {
  
  private val insertQuery = cqlt"insert into table (id, data) values (${Put[Int]}, ${Put[String]})"
    .config(_.setTimeout(1.second))
  private val selectQuery = cqlt"select id, data from table where id = ${Put[Int]}".as[Model]
  private val selectAllQuery = cqlt"select id, data from table".as[Model]

  def apply(session: CassandraSession) = for {
    insert <- insertQuery.prepare(session)
    select <- selectQuery.prepare(session)      
    selectAll <- selectAllQuery.prepare(session)
  } yield new Service {
    override def put(value: Model) = insert(value.id, value.data).execute.unit
    override def get(id: Int) = select(id).config(_.setExecutionProfileName("default")).selectOne
    override def getAll() = selectAll().config(_.setExecutionProfileName("default")).select
  } 
} 
```


### Raw API without cql
```scala
// Cassandra Session:
  val session = CassandraSession.make(config)
//OR
  val session = CassandraSession.make(cqlSessionBuilder)

// Use:
  val job = for {
    session  <- ZIO.service[CassandraSession]
    _        <- session.execute("insert ...")
    prepared <- session.prepare("select ...")
    select   <- session.bind(prepared, Seq(args))
    row      <- session.selectOne(select, profileName = "oltp")
  } yield row
  
  job.provideCustomLayer(CassandraSession.make(config).toLayer)

```


## References
- [Datastax Java driver](https://docs.datastax.com/en/developer/java-driver/latest/manual/core/)