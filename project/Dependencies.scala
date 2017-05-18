import sbt._

object Dependencies {

  val Version = "0.1-SNAPSHOT"
  val ScalaVersion = "2.12.1"
  val akkaVersion = "2.5.0"

  val Service = Seq(
    Akka.`akka-stream`,
    Akka.`akka-distributed-data`,
    Akka.`akka-remote`,
    Akka.`akka-cluster-tools`,
    Libs.`scala-java8-compat`,
    Libs.`scala-async`,
    Libs.`enumeratum`,
    Libs.`chill-akka`,
    Libs.`akka-management-cluster-http`,
    AkkaHttp.`akka-http`,
    Libs.`scalatest` % Test,
    Libs.`junit` % Test,
    Libs.`junit-interface` % Test,
    Libs.`mockito-core` % Test,
    Akka.`akka-stream-testkit` % Test,
    Akka.`akka-multi-node-testkit` % Test,
    "org.apache.zookeeper" % "zookeeper" % "3.5.2-alpha",
    "org.mousio" % "etcd4j" % "2.13.0"
  )
}