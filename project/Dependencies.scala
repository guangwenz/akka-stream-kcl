import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
  lazy val slf4j = "org.slf4j" % "slf4j-api" % "1.7.25"
  lazy val akkaStream = Seq(
    "com.typesafe.akka" %% "akka-stream" % "2.5.17",
    "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.17" % Test
  )
  lazy val kcl = "com.amazonaws" % "amazon-kinesis-client" % "1.9.2"

}
