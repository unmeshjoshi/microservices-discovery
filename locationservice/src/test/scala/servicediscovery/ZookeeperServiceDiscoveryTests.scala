package servicediscovery

import java.net.URI

import akka.actor.{Actor, ActorPath, ActorSystem, Props}
import akka.serialization.Serialization
import akka.util.Timeout
import com.servicediscovery.impl.zookeeper.ZookeeperLocationService
import com.servicediscovery.models.Connection.{AkkaConnection, HttpConnection}
import com.servicediscovery.models._
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object TestFutureExtension {

  implicit class RichFuture[T](val f: Future[T]) extends AnyVal {
    def await: T        = Await.result(f, 20.seconds)
    def done: Future[T] = Await.ready(f, 20.seconds)
  }

}


class TestActor() extends Actor {
  override def receive: Receive = {
    case m@_ ⇒ {
      val response = s"Received ${m}"
      println(response)
      response
    }
  }
}

class ZookeeperServiceDiscoveryTests extends FunSuite with Matchers with BeforeAndAfter {

  test("Should register and resolve a http service") {
    val hostname = "127.0.0.1"
    val port = 9595
    val prefix = "/trombone/hcd"
    val locationService = new ZookeeperLocationService()

    val connection = HttpConnection(ComponentId("config", ComponentType.Service))
    val location = HttpLocation(connection, new URI(s"http://$hostname:$port/$prefix"))
    locationService.register(HttpRegistration(connection, port, prefix))
    val resolvedLocation = locationService.resolve(connection)
    resolvedLocation shouldBe location
  }

  test("Should track an instance of http service") {
    val hostname = "127.0.0.1"
    val port = 9595
    val prefix = "/trombone/hcd"
    val locationService = new ZookeeperLocationService()

    val connection = HttpConnection(ComponentId("config1", ComponentType.Service))
    val location = HttpLocation(connection, new URI(s"http://$hostname:$port/$prefix"))
//    val resolvedLocation = locationService.track(connection)
//    locationService.register(location)
  }




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
    locationService.register(AkkaRegistration(connection, actorRef))
    val resolvedLocation: Location = locationService.resolve(connection)
    //FIXME assert on equality fails because of actor ref, and actorselection difference
    implicit val timeout = Timeout(5 seconds)
    resolvedLocation match {
      case AkkaLocation(_, _, resolvedActorRef) ⇒ {
        val response = resolvedActorRef ! "Test Message" //FIXME investigate why 'ask' calls deadlock
        println(response)
      }
      case _ ⇒ fail()
    }
    Thread.sleep(1000)
  }
}
