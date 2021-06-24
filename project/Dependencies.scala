import sbt._

object Dependencies {

  val cassandraDriverVersion = "4.12.0"

  val zioVersion = "1.0.9"

  val javaStreamsInterop = "1.3.5"

  val testContainersVersion = "0.39.5"

  val cassandraDependencies = Seq(
    "com.datastax.oss" % "java-driver-core" % cassandraDriverVersion
  )

  val zioDependencies = Seq(
    "dev.zio" %% "zio"                         % zioVersion,
    "dev.zio" %% "zio-streams"                 % zioVersion,
    "dev.zio" %% "zio-interop-reactivestreams" % javaStreamsInterop
  )

  val testCommon = Seq(
    "org.wvlet.airframe" %% "airframe-log"                   % "21.6.0",
    "org.slf4j"          % "slf4j-jdk14"                     % "1.7.31",
    "dev.zio"            %% "zio-test"                       % zioVersion,
    "dev.zio"            %% "zio-test-sbt"                   % zioVersion,
    "com.dimafeng"       %% "testcontainers-scala-core"      % testContainersVersion,
    "com.dimafeng"       %% "testcontainers-scala-cassandra" % testContainersVersion
  ).map(_ % Test)

}
