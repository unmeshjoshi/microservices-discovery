package com.account.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.account.model.AccountInfo
import spray.json._

import scala.io.StdIn

object Server extends App with SprayJsonSupport with DefaultJsonProtocol {

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher
  implicit val itemFormat = jsonFormat1(AccountInfo)

  val route =
    path("profile") {
      (get) {
        complete((StatusCodes.OK, new AccountInfo("12309123")))
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)

  println(s"Server online at http://0.0.0.0:8080/")

}
