package com.servicediscovery.impl.etcd

import akka.Done
import akka.stream.KillSwitch
import akka.stream.scaladsl.Source
import com.servicediscovery.models._
import com.servicediscovery.scaladsl.LocationService

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class EtcdZookeeperLocationService extends LocationService {
  override def register(registration: Registration): Future[RegistrationResult] = ???

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
