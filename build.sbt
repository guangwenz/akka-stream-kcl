import Dependencies._
import sbt.Keys.libraryDependencies

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "io.github.guangwenz",
      scalaVersion := "2.12.7",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Akka Stream KCL",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += slf4j,
    libraryDependencies ++= akkaStream,
    libraryDependencies += kcl
  )
  
useGpg := true