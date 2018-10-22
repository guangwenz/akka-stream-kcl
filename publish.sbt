ThisBuild / organization := "io.github.guangwenz"
ThisBuild / organizationHomepage := Some(url("https://github.com/guangwenz"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/guangwenz/akka-stream-kcl"),
    "scm:git@github.com:guangwenz/akka-stream-kcl.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id    = "guangwenz",
    name  = "Guangwen Zhou",
    email = "zgwmike@hotmail.com",
    url   = url("https://guangwenz.github.io")
  )
)

ThisBuild / description := "Akka Stream Source for AWS Kinesis Data Stream with KCL v2"
ThisBuild / licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/guangwenz/akka-stream-kcl"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true