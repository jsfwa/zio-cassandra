package zio.cassandra

package object cql {
  final implicit class CqlStringContext(private val ctx: StringContext) {
    val cqlt = new CqlTemplateStringInterpolator(ctx)
    val cql  = new CqlStringInterpolator(ctx)
  }
}
