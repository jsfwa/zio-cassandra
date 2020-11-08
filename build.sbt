ThisBuild / crossScalaVersions := Seq("2.13.3", "2.11.12", "2.12.12")

ThisBuild / scalaVersion := crossScalaVersions.value.head
name := "zio-cassandra"

lazy val connector =
  (project in file("connector"))
    .settings(
      testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
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
          Dependencies.testCommon ++
          Seq(
            "org.scala-lang.modules" %% "scala-collection-compat" % "2.2.0",
            "com.github.ghik"         % "silencer-lib"            % "1.7.1" % Provided cross CrossVersion.full,
            compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.1" cross CrossVersion.full)
          ),
      scalacOptions ++= Seq(
        "-encoding",
        "utf-8",
        "-feature",
        "-unchecked",
        "-deprecation"
      ) ++
        (scalaBinaryVersion.value match {
          case v if v.startsWith("2.13") =>
            List(
              "-P:silencer:globalFilters=JavaConverters",
              "-Xlint:-serial",
              "-Ywarn-unused",
              "-Ymacro-annotations",
              "-Yrangepos",
              "-Werror",
              "-explaintypes",
              "-language:higherKinds",
              "-language:implicitConversions",
              "-Xfatal-warnings",
              "-Wconf:any:error"
            )
          case v if v.startsWith("2.12") =>
            Nil
          case v if v.startsWith("2.11") =>
            List("-target:jvm-1.8")
          case v if v.startsWith("0.")   =>
            Nil
          case other                     => sys.error(s"Unsupported scala version: $other")
        }),
      publishArtifact in GlobalScope in Test := false,
      parallelExecution in Test := false,
      publishTo := sonatypePublishToBundle.value,
      releaseCrossBuild := true,
      releaseProcess := {
        import sbtrelease.ReleaseStateTransformations._
        Seq[ReleaseStep](
          inquireVersions,
          runClean,
          releaseStepCommandAndRemaining("+test"),
          setReleaseVersion,
          commitReleaseVersion,
          tagRelease,
          releaseStepCommandAndRemaining("+publishSigned"),
          releaseStepCommand("sonatypeBundleRelease"),
          setNextVersion,
          commitNextVersion,
          pushChanges
        )
      }
    )

lazy val root = (project in file("."))
  .aggregate(connector)
  .settings(
    skip in publish := true
  )
