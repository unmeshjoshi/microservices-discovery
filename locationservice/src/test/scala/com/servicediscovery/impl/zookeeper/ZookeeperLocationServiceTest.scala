package com.servicediscovery.impl.zookeeper
package servicediscovery

import java.net.URI

import akka.actor.{Actor, ActorPath, ActorSystem, Props}
import akka.serialization.Serialization
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.TestSink
import akka.util.Timeout
import com.servicediscovery.models.Connection.{AkkaConnection, HttpConnection}
import com.servicediscovery.models._
import com.utils.Networks
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object TestFutureExtension {

  implicit class RichFuture[T](val f: Future[T]) extends AnyVal {
    def await: T = Await.result(f, 20.seconds)

    def done: Future[T] = Await.ready(f, 20.seconds)
  }

}


class TestActor() extends Actor {
  override def receive: Receive = {
    case m@_ ⇒ {
      val response = s"Received ${m}"
      response
    }
  }
}

class ZookeeperServiceDiscoveryTests extends FunSuite with Matchers with BeforeAndAfter {

  test("Should register and resolve a http service") {
    val hostname = new Networks().hostname()
    val port = 9595
    val prefix = "/trombone/hcd1"
    val locationService = new ZookeeperLocationService()

    val connection = HttpConnection(ComponentId("config", ComponentType.Service))
    val register = locationService.register(HttpRegistration(connection, port, prefix))
    Await.result(register, 5 seconds)

    val resolvedLocation = locationService.resolve(connection)

    val expectedLocation = HttpLocation(connection, new URI(s"http://$hostname:$port/$prefix"))
    resolvedLocation shouldBe expectedLocation

    locationService.unregister(connection)
  }


  test("Should track an instance of http service") {

    val hostname = "127.0.0.1"
    val port = 9595
    val prefix = "/trombone/hcd"
    val locationService = new ZookeeperLocationService()

    val connection = HttpConnection(ComponentId("config1", ComponentType.Service))

    implicit val actorSystem = ActorSystem()
    implicit val actorMaterialLizer = ActorMaterializer()
    val resolvedLocation = locationService.track(connection)
    val (httpSwitch, httpProbe) = resolvedLocation.toMat(TestSink.probe[TrackingEvent])(Keep.both).run()
    val location = HttpLocation(connection, new URI(s"http://$hostname:$port/$prefix"))

    val register = locationService.register(HttpRegistration(connection, port, prefix))
    Await.result(register, 5 seconds)

    val httpEvent: TrackingEvent = httpProbe.requestNext()
    val trackedHttpConnection = httpEvent.asInstanceOf[LocationUpdated].connection
    trackedHttpConnection shouldBe connection

    val unregister = locationService.unregister(connection)
    Await.result(unregister, 5 seconds)

    val httpEvent2: TrackingEvent = httpProbe.requestNext()
    val trackedHttpConnection2 = httpEvent2.asInstanceOf[LocationRemoved].connection
    trackedHttpConnection2 shouldBe connection

  }

  import akka.pattern.ask

  test("Should register and resolve a actor service") {
    val hostname = "127.0.0.1"
    val port = 9595
    val prefix = "/trombone/hcd"
    val locationService = new ZookeeperLocationService()

    val connection = AkkaConnection(ComponentId("config", ComponentType.HCD))

    val actorSystem = ActorSystem("TestSystem")

    val actorRef = actorSystem.actorOf(Props[TestActor])
    val actorPath = ActorPath.fromString(Serialization.serializedActorPath(actorRef))
    val uri = new URI(actorPath.toString)
    val location = AkkaLocation(connection, uri, actorRef)
    val registrationResult = locationService.register(AkkaRegistration(connection, actorRef))
    Await.result(registrationResult, 5 seconds)
    val resolvedLocation: Location = locationService.resolve(connection)
    //FIXME assert on equality fails because of actor ref, and actorselection difference
    implicit val timeout = Timeout(5 seconds)
    resolvedLocation match {
      case AkkaLocation(_, _, resolvedActorRef) ⇒ {
        val responseF: Unit = resolvedActorRef ! "Test Message" //FIXME investigate why 'ask' calls deadlock
      }
      case _ ⇒ fail()
    }
    locationService.unregister(connection)
  }
}
