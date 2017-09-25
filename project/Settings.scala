import sbt.Keys._
import sbt._

//noinspection TypeAnnotation
// Defines the global build settings so they don't need to be edited everywhere
object Settings {

  val buildSettings = Seq(
    organization := "org.tmt",
    organizationName := "TMT",
    organizationHomepage := Some(url("http://www.tmt.org")),
    version := Dependencies.Version,
    scalaVersion := Libs.ScalaVersion,
    crossPaths := true,
    parallelExecution in Test := false,
    fork := true
  )

  lazy val defaultSettings = buildSettings ++ Seq(
    // compile options ScalaUnidoc, unidoc
    scalacOptions ++= Seq("-target:jvm-1.8", "-encoding", "UTF-8", "-feature", "-deprecation", "-unchecked"),
    javacOptions in Compile ++= Seq("-source", "1.8"),
    javacOptions in(Compile, compile) ++= Seq("-add-modules","java.xml.bind","-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation"),
    javaOptions in(Test, run) ++= Seq("-Djava.net.preferIPv4Stack=true") // For location service use

  )

}
