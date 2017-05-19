package com.servicediscovery.impl.zookeeper

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import akka.Done
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.{KillSwitch, KillSwitches, OverflowStrategy}
import akka.stream.scaladsl.{Keep, Source}
import com.servicediscovery.exceptions.RegistrationFailed
import com.servicediscovery.models._
import com.servicediscovery.scaladsl.LocationService
import com.utils.Networks
import org.apache.zookeeper.Watcher.Event.EventType
import org.apache.zookeeper.ZooDefs.Perms
import org.apache.zookeeper.data.{ACL, Id}
import org.apache.zookeeper.{CreateMode, WatchedEvent, ZooKeeper}

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class ZookeeperLocationService extends LocationService { outer =>
  private val actorSystem = ActorSystem("LocationServiceActorSystem")
  private val cswRootPath = "/csw"

  import scala.concurrent.ExecutionContext.Implicits.global

  override def register(registration: Registration): Future[RegistrationResult] = Future {
    val location = registration.location(new Networks().hostname())
    val serviceInstancePath = s"$cswRootPath/${location.connection.name}"
    val stat = zookeeper.exists(serviceInstancePath, false)
    if (Option(stat) != None) {
      throw RegistrationFailed(registration.connection)
    }

    val data = serialize(location)
    if (Option(zookeeper.exists(cswRootPath, false)) == None) {
      zookeeper.create(cswRootPath, data, aclList.asJava, CreateMode.PERSISTENT)
    }
    val createdNode: String = zookeeper.create(serviceInstancePath, data, aclList.asJava, CreateMode.EPHEMERAL)
    println(s"${createdNode} registration complete")
    registrationResult(location)
  }

  override def unregister(connection: Connection): Future[Done] = Future {
    val serviceInstancePath = s"$cswRootPath/${connection.name}"
    val anyVersion = -1
    zookeeper.delete(serviceInstancePath, anyVersion)
    println(s"Unregistered ${serviceInstancePath}")
    Done
  }

  private def registrationResult(loc: Location): RegistrationResult = new RegistrationResult {
    override def location: Location = loc

    override def unregister(): Future[Done] = ??? //outer.unregister(location.connection)
  }

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

  val watcher = new LocationWatcher
  val zookeeper = new ZooKeeper("localhost:2181", 60 * 1000, watcher)
  val zkId: Id = new Id("world", "anyone");
  val acl = new ACL(Perms.ALL, zkId)
  val aclList = List[_root_.org.apache.zookeeper.data.ACL](acl)

  def resolve(connection: Connection): Location = {
    try {
      val serviceInstancePath = s"${cswRootPath}/${connection.name}"
      val serviceDetails: Array[Byte] = zookeeper.getData(serviceInstancePath, false, null)
      val location = deserialize(serviceDetails)
      location
    } catch {
      case e:Exception  ⇒ {
        e.printStackTrace()
        throw e
      }
    }
  }


  override def track(connection: Connection): Source[TrackingEvent, akka.stream.KillSwitch] = {
    val serviceInstancePath = s"$cswRootPath/${connection.name}"

    def setWatcher(actorRef: ActorRef):Unit = {
      zookeeper.exists(s"${serviceInstancePath}", watchedEvent ⇒ {
        actorRef ! watchedEvent
        setWatcher(actorRef)
      })
    }

    val source: Source[Any, Unit] = Source.actorRef[Any](256, OverflowStrategy.fail).mapMaterializedValue {
      actorRef ⇒
        val exists = setWatcher(actorRef)
    }

    def asLocationUpdated(w: WatchedEvent) = {
      val resolve1 = resolve(connection)
      LocationUpdated(resolve1)

    }

    def asLocationRemoved(w: WatchedEvent) = {
      LocationRemoved(connection)
    }

    val collect: Source[TrackingEvent, Unit] = source.collect {
      case w: WatchedEvent if w.getType() == EventType.NodeCreated ⇒ asLocationUpdated(w)
      case w: WatchedEvent if w.getType() == EventType.NodeDeleted ⇒ asLocationRemoved(w)
    }
    collect.viaMat(KillSwitches.single)(Keep.right)
  }

  override def unregisterAll(): Future[Done] = ???

  override def find(connection: Connection): Future[Option[Location]] = ???

  override def resolve(connection: Connection, within: FiniteDuration): Future[Option[Location]] = Future {
    val location = resolve(connection)
    Option(location)
  }

  override def list: Future[List[Location]] = ???

  override def list(componentType: ComponentType): Future[List[Location]] = ???

  override def list(hostname: String): Future[List[Location]] = ???

  override def list(connectionType: ConnectionType): Future[List[Location]] = ???

  override def subscribe(connection: Connection, callback: (TrackingEvent) ⇒ Unit): KillSwitch = ???

  override def shutdown(): Future[Done] = ???
}

class LocationWatcher extends org.apache.zookeeper.Watcher {
  override def process(event: WatchedEvent): Unit = {

  }
}
