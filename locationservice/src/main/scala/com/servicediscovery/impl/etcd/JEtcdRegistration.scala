package com.servicediscovery.impl.etcd

import java.net.InetAddress
import java.util

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import akka.stream.{ActorMaterializer, Attributes, Outlet, SourceShape}
import com.coreos.jetcd.Client
import com.coreos.jetcd.data.ByteSequence
import com.coreos.jetcd.watch.{WatchEvent, WatchResponse}
import com.utils.Networks

import scala.collection.immutable.Queue
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EtcdWatchingGraphStage extends GraphStage[SourceShape[WatchEvent]] {
  val out = Outlet[WatchEvent]("etcd.out")

  override def shape = SourceShape.of(out)

  override def createLogic(inheritedAttributes: Attributes) = new GraphStageLogic(shape) with OutHandler {
    var buffer = Queue[WatchEvent]() //TODO: Possibly use Source.queue
    override def onPull(): Unit = {
      if (!buffer.isEmpty) {
        pushIfReady
      } else {
        val callback = getAsyncCallback(handleEtcdResponse)
        val etcdFuture = new EtcdService().watch("/skydns/local/skydns/service/account/1")
        etcdFuture.foreach(response ⇒ callback.invoke(response))

      }
    }

    private def pushIfReady = {
      if (isAvailable(out)) {
        val value: (WatchEvent, Queue[WatchEvent]) = buffer.dequeue
        buffer = value._2
        println(s"Output port available. Pushing value ${value._1}")
        push(out, value._1)
      }
    }

    private def handleEtcdResponse(events: util.List[WatchEvent]): Unit = {
      import scala.collection.JavaConverters._
      println(s"Adding BUFFER ${events}")
      buffer = buffer ++ events.asScala
      pushIfReady
    }

    setHandler(out, this)
  }
}

class EtcdService {
  val hostIp: InetAddress = new Networks().ipv4Address //get ip address of primary interface.
  def url(hostIp: InetAddress, port: Int) = s"http:/${hostIp}:${port}"

  val etcdClient = Client.builder.endpoints(url(hostIp, 4002), url(hostIp, 4001)).build

  def watch(key: String): Future[util.List[WatchEvent]] = Future {
    val watchClient = etcdClient.getWatchClient
    import com.coreos.jetcd.data.ByteSequence
    val watcher = watchClient.watch(ByteSequence.fromString(key))
    println("waiting for value")
    val response: WatchResponse = watcher.listen()
    response.getEvents
  }
}

object JEtcdRegistration extends App {
  new JEtcdRegistration().register
}

class JEtcdRegistration {


  val hostIp: InetAddress = new Networks().ipv4Address //get ip address of primary interface.
  def url(hostIp: InetAddress, port: Int) = s"http:/${hostIp}:${port}"

  println(s"host ip address is ${hostIp}")
  implicit val actorSystem = ActorSystem("etcdSystem")
  implicit val actorMaterializer = ActorMaterializer()

  def register = {
    //
    //    val eventualEvents = new EtcdService().watch("/skydns/local/skydns/service/account/1")
    //    eventualEvents.foreach(event⇒println(event))

    Source.fromGraph(new EtcdWatchingGraphStage).runForeach(watchEvent ⇒ println(s"---------------------------------------------------------------------------------${watchEvent.getEventType}:${watchEvent.getKeyValue.getKey}=${watchEvent.getKeyValue.getValue}"))

    var i = 0;
    val etcdClient = Client.builder.endpoints(url(hostIp, 4002), url(hostIp, 4001)).build
    while (true) {
      val ip4Address = new Networks().ipv4Address.getHostAddress
      etcdClient.getKVClient.put(ByteSequence.fromString("/skydns/local/skydns/service/account/1"),
        ByteSequence.fromString(s"""{ "host":"${ip4Address}", "port":8080, "priority": 20, "txt":"${i}"}"""))
      println("registered value")
      i = i + 1
      Thread.sleep(4000)
    }

    if (etcdClient != null) etcdClient.close()

  }

}
