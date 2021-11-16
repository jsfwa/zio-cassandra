name := "zio-cassandra"

inThisBuild(
  List(
    organization := "io.github.jsfwa",
    scalaVersion := "2.13.7",
    crossScalaVersions := Seq("2.13.7", "2.12.15"),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer("jsfwa", "jsfwa", "zubrilinandrey@gmail.com", url("https://gitlab.com/jsfwa")),
      Developer("alzo", "Sergey Rublev", "alzo@alzo.space", url("https://github.com/narma/"))
    ),
    scmInfo := Some(ScmInfo(url("https://github.com/jsfwa/zio-cassandra"), "git@github.com:jsfwa/zio-cassandra.git")),
    homepage := Some(url("https://github.com/jsfwa/zio-cassandra"))
  )
)

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))


def commonScalacOptions: Seq[String] = Seq(
  "-encoding",
  "utf-8",
  "-unchecked",
  "-explaintypes",
  "-Yrangepos",
  "-Ywarn-unused",
  "-deprecation",
  "-language:higherKinds",
  "-language:implicitConversions"
)

lazy val root =
  (project in file("."))
    .settings(
      libraryDependencies ++=
        Dependencies.cassandraDependencies ++
          Dependencies.zioDependencies ++
          Dependencies.testCommon ++ (scalaBinaryVersion.value match {
          case v if v.startsWith("2.12") =>
            Seq("org.scala-lang.modules" %% "scala-collection-compat" % "2.6.0")
          case _ => Seq.empty
        })
      ,
      scalacOptions ++= commonScalacOptions ++ (scalaBinaryVersion.value match {
        case v if v.startsWith("2.13") =>
          List(
            "-Xlint:strict-unsealed-patmat",
            "-Xlint:-serial",
            "-Ymacro-annotations",
            "-Werror",
            "-Xfatal-warnings",
            "-Wconf:any:error"
          )
        case v if v.startsWith("2.12") =>
          List(
            "-language:higherKinds",
            "-Xfatal-warnings"
          )
        case v if v.startsWith("0.")   =>
          Nil
        case other                     => sys.error(s"Unsupported scala version: $other")
      }),
      Test / parallelExecution := false,
      Test / fork := true
    )

