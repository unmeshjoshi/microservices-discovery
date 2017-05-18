package com.servicediscovery.scaladsl

import akka.Done
import akka.stream.KillSwitch
import akka.stream.scaladsl.Source
import com.servicediscovery.models._

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

trait LocationService {

  def register(registration: Registration): Future[RegistrationResult]

  def unregister(connection: Connection): Future[Done]

  def unregisterAll(): Future[Done]

  def find(connection: Connection): Future[Option[Location]]

  def resolve(connection: Connection, within: FiniteDuration): Future[Option[Location]]

  def list: Future[List[Location]]

  def list(componentType: ComponentType): Future[List[Location]]

  def list(hostname: String): Future[List[Location]]

  def list(connectionType: ConnectionType): Future[List[Location]]

  def track(connection: Connection): Source[TrackingEvent, KillSwitch]

  def subscribe(connection: Connection, callback: TrackingEvent ⇒ Unit): KillSwitch

  def shutdown(): Future[Done]
}
