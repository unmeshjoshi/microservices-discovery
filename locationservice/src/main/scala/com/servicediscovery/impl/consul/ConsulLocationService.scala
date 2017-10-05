package com.servicediscovery.impl.consul

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import akka.Done
import akka.actor.ActorSystem
import com.ecwid.consul.v1.kv.model.GetBinaryValue
import com.ecwid.consul.v1.{ConsulClient, QueryParams, Response}
import com.servicediscovery.exceptions.RegistrationFailed
import com.servicediscovery.models._
import com.servicediscovery.scaladsl.LocationService
import com.utils.Networks

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class ConsulLocationService extends LocationService {
  private val actorSystem = ActorSystem("LocationServiceActorSystem")
  private val cswRootPath = "/csw"

  import scala.concurrent.ExecutionContext.Implicits.global

  val consulClient = new ConsulClient("127.0.0.1", 8500)

  override def register(registration: Registration) = Future {
    val location = registration.location(new Networks().hostname())
    val serviceInstancePath = s"$cswRootPath/${location.connection.name}"
    val waitTime = 0
    val queryParams = new QueryParams(waitTime, 0)
    val registeredService = consulClient.getKVBinaryValue(serviceInstancePath, queryParams)
    if (Option(registeredService.getValue) == None) {
      throw RegistrationFailed(registration.connection)
    }

    consulClient.setKVBinaryValue(serviceInstancePath, serialize(location))
    registrationResult(location)
  }

  override def unregister(connection: Connection) = Future {
    val serviceInstancePath = s"$cswRootPath/${connection.name}"
    consulClient.deleteKVValue(serviceInstancePath)
    Done
  }

  override def unregisterAll() = ???

  override def find(connection: Connection) = Future {
    try {
      val serviceInstancePath = s"${cswRootPath}/${connection.name}"
      val serviceDetails = consulClient.getKVBinaryValue(serviceInstancePath)
      getLocation(serviceDetails)
    } catch {
      case e: Exception ⇒ {
        e.printStackTrace()
        throw e
      }
    }
  }

  private def getLocation(serviceDetails: Response[GetBinaryValue]):Option[Location] = {
    if (Option(serviceDetails.getValue) == None) {
      None
    } else {
      val location = deserialize(serviceDetails.getValue.getValue)
      Some(location)
    }
  }

  override def resolve(connection: Connection, within: FiniteDuration) = Future {
    try {
      val serviceInstancePath = s"${cswRootPath}/${connection.name}"
      var serviceDetails = consulClient.getKVBinaryValue(serviceInstancePath)
      if (Option(serviceDetails.getValue) == None) {
          val consulIndex = serviceDetails.getConsulIndex
          System.out.println(s"${connection.name} not registered. Current Index is " + consulIndex + s" . Waiting for it to be registered for ${within}")
          val waitTime = within.toMillis
          serviceDetails = consulClient.getKVBinaryValue(serviceInstancePath, new QueryParams(waitTime, consulIndex))
        }

      getLocation(serviceDetails)
    } catch {
      case e: Exception ⇒ {
        e.printStackTrace()
        throw e
      }
    }
  }

  override def list = ???

  override def list(componentType: ComponentType) = ???

  override def list(hostname: String) = ???

  override def list(connectionType: ConnectionType) = ???

  override def track(connection: Connection) = {

  }

  override def subscribe(connection: Connection, callback: (TrackingEvent) ⇒ Unit) = ???

  override def shutdown() = ???

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

  private def registrationResult(loc: Location): RegistrationResult = new RegistrationResult {
    override def location: Location = loc

    override def unregister(): Future[Done] = ??? //outer.unregister(location.connection)
  }

}
