package zio.cassandra.cql

object syntax {
  final implicit class CqlStringContext(private val ctx: StringContext) {
    val cqlt = new CqlTemplateStringInterpolator(ctx)
    val cql  = new CqlStringInterpolator(ctx)
  }
}
