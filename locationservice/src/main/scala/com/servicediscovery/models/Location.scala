package com.servicediscovery.models

import java.io.{ObjectInputStream, ObjectOutputStream}
import java.net.URI

import akka.actor.{ActorRef, ActorSystem, ExtendedActorSystem}
import akka.serialization.Serialization
import com.servicediscovery.models.Connection.{AkkaConnection, HttpConnection, TcpConnection}

sealed abstract class Location extends TmtSerializable {
  def connection: Connection
  def uri: URI
}

final case class AkkaLocation(var connection: AkkaConnection, var uri: URI, var actorRef: ActorRef) extends Location {

  private def readObject(arg: ObjectInputStream): Unit = { //FIXME actorref serialization and deserialization should be done outside
    val actorSystem = ActorSystem("ForDeserialization").asInstanceOf[ExtendedActorSystem]
    import akka.serialization.SerializationExtension
    val serialization = SerializationExtension.get(actorSystem)

    connection = arg.readObject().asInstanceOf[AkkaConnection]
    uri = arg.readObject().asInstanceOf[URI]
    val serializedActorPath = arg.readObject().asInstanceOf[String]
    actorRef = actorSystem.provider.resolveActorRef(serializedActorPath)
  }

  private def writeObject(arg: ObjectOutputStream): Unit = {
    arg.writeObject(connection)
    arg.writeObject(uri)
    arg.writeObject(Serialization.serializedActorPath(actorRef))
  }
}

final case class TcpLocation(connection: TcpConnection, uri: URI) extends Location

final case class HttpLocation(connection: HttpConnection, uri: URI) extends Location
