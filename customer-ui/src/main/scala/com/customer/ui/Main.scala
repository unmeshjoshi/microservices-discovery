package com.customer.ui

import akka.actor.ActorSystem

object Main extends App {

  implicit val system = ActorSystem()

  new CustomerServiceGateway().customerInfo
  new AccountServiceGateway().accountInfo

  system.terminate()

}
