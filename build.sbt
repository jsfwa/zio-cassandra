lazy val connector =
  (project in file("connector"))
    .settings(
      testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
      scalaVersion := "3.0.0",
      organization := "io.github.jsfwa",
      homepage := Some(url("https://github.com/jsfwa/zio-cassandra")),
      scmInfo := Some(ScmInfo(url("https://github.com/jsfwa/zio-cassandra"), "git@github.com:jsfwa/zio-cassandra.git")),
      developers := List(
        Developer("jsfwa", "jsfwa", "zubrilinandrey@gmail.com", url("https://gitlab.com/jsfwa")),
        Developer("alzo", "Sergey Rublev", "alzo@alzo.space", url("https://github.com/narma/"))
      ),
      licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
      name := "zio-cassandra",
      libraryDependencies ++=
        Dependencies.cassandraDependencies ++
          Dependencies.zioDependencies ++
          Dependencies.testCommon,
      scalacOptions ++= Seq(
        "-encoding",
        "utf-8",
        "-unchecked",
        "-indent",
        "-rewrite",
        "-deprecation",
        "-language:higherKinds",
        "-language:implicitConversions",
        "-Xfatal-warnings"
      ),
      Test / publishArtifact := false,
      Test / parallelExecution := false,
      Test / fork := true
    )

lazy val root = (project in file("."))
  .aggregate(connector)
  .settings(
    publish / skip := true
  )
