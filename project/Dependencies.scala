import sbt._

object Dependencies {
  val cassandraDriverVersion = "4.9.0"
  val zioVersion             = "1.0.3"
  val javaStreamsInterop     = "1.0.3.5"

  val cassandraDependencies = Seq(
    "com.datastax.oss" % "java-driver-core" % cassandraDriverVersion
  )

  val zioDependencies = Seq(
    "dev.zio" %% "zio"                         % zioVersion,
    "dev.zio" %% "zio-streams"                 % zioVersion,
    "dev.zio" %% "zio-interop-reactivestreams" % javaStreamsInterop
  )

  val testCommon = Seq(
    "org.wvlet.airframe" %% "airframe-log"                   % "20.10.0",
    "org.slf4j"           % "slf4j-jdk14"                    % "1.7.30",
    "dev.zio"            %% "zio-test"                       % zioVersion,
    "dev.zio"            %% "zio-test-sbt"                   % zioVersion,
    "com.dimafeng"       %% "testcontainers-scala-core"      % "0.38.4",
    "com.dimafeng"       %% "testcontainers-scala-cassandra" % "0.38.4",
    // see https://github.com/testcontainers/testcontainers-java/issues/3166
    "org.testcontainers"  % "testcontainers"                 % "1.15.0-rc2"
  ).map(_ % Test)

}
