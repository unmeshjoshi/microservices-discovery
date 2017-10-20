package com.servicediscovery.impl.etcd

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}
import java.net.InetAddress
import java.util

import akka.Done
import akka.actor.ActorSystem
import akka.stream.{Attributes, KillSwitch, OverflowStrategy, SourceShape}
import akka.stream.scaladsl.Source
import akka.stream.stage.{GraphStage, GraphStageLogic}
import com.coreos.jetcd.Client
import com.coreos.jetcd.data.ByteSequence
import com.coreos.jetcd.watch.WatchEvent
import com.servicediscovery.models._
import com.servicediscovery.scaladsl.LocationService
import com.utils.Networks

import scala.compat.java8.FutureConverters._
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext.Implicits.global

class EtcdLocationService extends LocationService { outer ⇒
  private val actorSystem = ActorSystem("LocationServiceActorSystem")
  private val cswRootPath = "/csw"

  val hostIp: InetAddress = new Networks().ipv4Address //get ip address of primary interface.
  def url(hostIp: InetAddress, port: Int) = s"http:/${hostIp}:${port}"

  val etcdClient = Client.builder.endpoints(url(hostIp, 4002), url(hostIp, 4001)).build

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

  override def register(registration: Registration): Future[RegistrationResult] = {

    val hostName = new Networks().hostname()
    val location = registration.location(hostName)
    val kvClient = etcdClient.getKVClient
    val result = kvClient.put(ByteSequence.fromString(serviceInstanceKey(location.connection)),
    ByteSequence.fromBytes(serialize(location)))
    val scalaFuture = result.toScala
    scalaFuture.map(f ⇒ {
      registrationResult(location)
    })
  }

  private def registrationResult(loc: Location): RegistrationResult = new RegistrationResult {
    override def location: Location = loc

    override def unregister(): Future[Done] = outer.unregister(loc.connection)
  }

  override def unregister(connection: Connection): Future[Done] = ???

  override def unregisterAll(): Future[Done] = ???

  override def find(connection: Connection): Future[Option[Location]] = {
    val locationF = etcdClient.getKVClient.get(ByteSequence.fromString(serviceInstanceKey(connection)))
    locationF.toScala.map(getResponse ⇒ {
      val kvs = getResponse.getKvs
      if (kvs.size() > 0) {
        val value: ByteSequence = kvs.get(0).getValue
        Some(deserialize(value.getBytes))
      }
      else None
    })
  }

  private def serviceInstanceKey(connection: Connection) = {
    s"$cswRootPath/${connection.name}"
  }

  override def resolve(connection: Connection, within: FiniteDuration): Future[Option[Location]] = ???

  override def list: Future[List[Location]] = ???

  override def list(componentType: ComponentType): Future[List[Location]] = ???

  override def list(hostname: String): Future[List[Location]] = ???

  override def list(connectionType: ConnectionType): Future[List[Location]] = ???

  override def subscribe(connection: Connection, callback: (TrackingEvent) ⇒ Unit): KillSwitch = ???

  override def shutdown(): Future[Done] = ???

  override def track(connection: Connection) = ???
}
