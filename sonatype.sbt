// Your profile name of the sonatype account. The default is the same with the organization value
sonatypeProfileName := "io.github.jsfwa"

// To sync with Maven central, you need to supply the following information:
publishMavenStyle := true

// Open-source license of your choice
licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

// Where is the source code hosted: GitHub or GitLab?
import sbt.Developer
import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting("jsfwa", "zio-cassandra", "zubrilinandrey@gmail.com"))

// or if you want to set these fields manually
homepage := Some(url("https://github.com/jsfwa/zio-cassandra"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/jsfwa/zio-cassandra"),
    "scm:git@github.com:jsfwa/zio-cassandra.git"
  )
)
developers := List(
  Developer(id = "jsfwa", name = "jsfwa", email = "zubrilinandrey@gmail.com", url = url("https://gitlab.com/jsfwa")),
  Developer(id = "alzo", name = "alzo", email = "alzo@alzo.space", url = url("https://alzo.space"))
)
