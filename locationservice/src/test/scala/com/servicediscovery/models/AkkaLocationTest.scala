package com.servicediscovery.models

import java.net.URI

import akka.actor.{Actor, ActorPath, ActorSystem, Props}
import akka.serialization.Serialization
import com.servicediscovery.impl.zookeeper.ZookeeperLocationService
import com.servicediscovery.impl.zookeeper.servicediscovery.TestActor
import com.servicediscovery.models.Connection.AkkaConnection
import com.utils.Networks
import org.scalatest.FunSuite


class TestActor1() extends Actor {
  override def receive: Receive = {
    case m@_ ⇒ {
      val response = s"Received ${m}"
      response
    }
  }
}

class AkkaLocationTest extends FunSuite {



  test("should be able to serialize and deserialize actor locations") {
    val hostname = new Networks().ipv4Address
    val port = 9595
    val prefix = "/trombone/hcd"

    val connection = AkkaConnection(ComponentId("config", ComponentType.HCD))
    val actorSystem = ActorSystem("TestSystem")

    val actorRef = actorSystem.actorOf(Props[TestActor1])
    val str: String = Serialization.serializedActorPath(actorRef)
    val actorPath = ActorPath.fromString(str)
    val uri = new URI(actorPath.toString)
    val location = AkkaLocation(connection, uri, actorRef)

    val ref = ActorSerializationExt.actorSystem.provider.resolveActorRef(str.toString)
    println(ref)



  }

}
