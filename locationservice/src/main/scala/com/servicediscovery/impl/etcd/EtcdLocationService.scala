package com.servicediscovery.impl.etcd

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}
import java.net.URI

import akka.Done
import akka.actor.ActorSystem
import akka.stream.KillSwitch
import akka.stream.scaladsl.Source
import com.servicediscovery.models._
import com.servicediscovery.scaladsl.LocationService
import com.utils.Networks
import mousio.etcd4j.EtcdClient
import mousio.etcd4j.promises.EtcdResponsePromise
import mousio.etcd4j.responses.EtcdKeysResponse

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class EtcdLocationService extends LocationService {
  private val actorSystem = ActorSystem("LocationServiceActorSystem")
  private val cswRootPath = "/csw"

  import scala.concurrent.ExecutionContext.Implicits.global

  val etcd = new EtcdClient(URI.create("http://192.168.0.111:4002"))

  def serialize(location: Location) = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(stream)
    oos.writeObject(location)
    oos.close
    stream.toByteArray
  }

  def deserialize(byteArray: Array[Byte]): Location = {
    val ois = new ObjectInputStream(new ByteArrayInputStream(byteArray))
    val location = ois.readObject().asInstanceOf[Location]
    println(s"++++++++++++++++++++++deserialized location $location++++++++++++")
    location
  }

  override def register(registration: Registration): Future[RegistrationResult] = Future {
    val location = registration.location(new Networks().hostname())
    val serviceInstancePath = s"$cswRootPath/${location.connection.name}"
    val send: EtcdResponsePromise[EtcdKeysResponse] = etcd.put(serviceInstancePath, new String(serialize(location))).send()
    registrationResult(location)
  }

  private def registrationResult(loc: Location): RegistrationResult = new RegistrationResult {
    override def location: Location = loc

    override def unregister(): Future[Done] = ??? //outer.unregister(location.connection)
  }

  override def unregister(connection: Connection): Future[Done] = ???

  override def unregisterAll(): Future[Done] = ???

  override def find(connection: Connection): Future[Option[Location]] = ???

  override def resolve(connection: Connection, within: FiniteDuration): Future[Option[Location]] = ???

  override def list: Future[List[Location]] = ???

  override def list(componentType: ComponentType): Future[List[Location]] = ???

  override def list(hostname: String): Future[List[Location]] = ???

  override def list(connectionType: ConnectionType): Future[List[Location]] = ???

  override def track(connection: Connection): Source[TrackingEvent, KillSwitch] = ???

  override def subscribe(connection: Connection, callback: (TrackingEvent) ⇒ Unit): KillSwitch = ???

  override def shutdown(): Future[Done] = ???
}
