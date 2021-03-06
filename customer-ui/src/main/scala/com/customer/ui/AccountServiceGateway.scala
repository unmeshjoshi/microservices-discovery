package com.customer.ui

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, RunnableGraph, Sink, Source}
import akka.util.ByteString
import spray.json._

import scala.concurrent.Await
import scala.concurrent.duration._

class AccountServiceGateway {

  def accountInfo(implicit actorSystem:ActorSystem) = {
    import scala.concurrent.Future
    implicit val materializer = ActorMaterializer.create(actorSystem)

    val responseFuture: Future[HttpResponse] =
      Http(actorSystem).singleRequest(HttpRequest(uri = "http://localhost:8080/profile"))
    val customnerInfoSource: Source[ByteString, Any] = Await.result(responseFuture, 5 seconds).entity.dataBytes

    var array = Array[ByteString]()
    val sink: Sink[ByteString, Future[Done]] = Sink.foreach((bytes: ByteString) => {
     array = array :+ bytes
    })
    val graph: RunnableGraph[Future[Done]] = customnerInfoSource.toMat(sink)(Keep.right)

    def readFuture: Future[Done] = graph.run()

    val result = Await.result(readFuture, 5 seconds)
    val value: JsValue = JsonParser(ParserInput(array(0).toArray))
    println(value)
  }
}
