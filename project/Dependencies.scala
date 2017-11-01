import sbt._

object Dependencies {

  val Version = "0.1-SNAPSHOT"
  val Service = Seq(
    Akka.`akka-stream`,
    Akka.`akka-distributed-data`,
    Akka.`akka-remote`,
    Akka.`akka-cluster-tools`,
    Libs.`scala-java8-compat`,
    Libs.`scala-async`,
    Enumeratum.`enumeratum`,
    Chill.`chill-akka`,
    Libs.`akka-management-cluster-http`,
    AkkaHttp.`akka-http`,
    Libs.`scalatest` % Test,
    Libs.`junit` % Test,
    Libs.`junit-interface` % Test,
    Libs.`mockito-core` % Test,
    Akka.`akka-stream-testkit` % Test,
    Akka.`akka-multi-node-testkit` % Test,
    "org.apache.zookeeper" % "zookeeper" % "3.5.3-beta",
    "com.ecwid.consul" % "consul-api" % "1.2.4",
    "com.coreos" % "jetcd-core" % "0.0.1",
    "com.dimafeng" % "testcontainers-scala_2.12" % "0.4.0" % "test" // Scala 2.12.*

  )
}