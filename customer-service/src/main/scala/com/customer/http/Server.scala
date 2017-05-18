package com.account.http
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.customer.model.CustomerInfo
import spray.json._

import scala.io.StdIn

object Server extends App with SprayJsonSupport with DefaultJsonProtocol {

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher
  implicit val itemFormat = jsonFormat2(CustomerInfo)
  val route =
    path("customer") {
      (get) {
        complete((StatusCodes.OK, new CustomerInfo("Name", "Address")))
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8081)

  println(s"Server online at http://localhost:8080/")
}
