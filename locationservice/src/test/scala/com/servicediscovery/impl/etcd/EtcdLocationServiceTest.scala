package com.servicediscovery.impl.etcd

import java.net.URI

import com.servicediscovery.models.Connection.HttpConnection
import com.servicediscovery.models.{ComponentId, ComponentType, HttpLocation, HttpRegistration}
import com.utils.Networks
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class EtcdLocationServiceTest extends FunSuite with Matchers {


  test("Should register and resolve a http service") {
    val hostname = new Networks().hostname()
    val port = 9595
    val prefix = "/trombone/hcd1"

    val locationService = new EtcdLocationService()
    val connection = HttpConnection(ComponentId("config", ComponentType.Service))
    val register = locationService.register(HttpRegistration(connection, port, prefix))
    Await.result(register, 1 seconds)

    val resolvedLocation = locationService.find(connection)
    Await.result(resolvedLocation, 1 seconds)

    val expectedLocation = HttpLocation(connection, new URI(s"http://$hostname:$port/$prefix"))
    Await.result(resolvedLocation, 1 second) shouldBe expectedLocation

    locationService.unregister(connection)
  }

  test("testFind") {

  }

  test("testTrack") {

  }

}
