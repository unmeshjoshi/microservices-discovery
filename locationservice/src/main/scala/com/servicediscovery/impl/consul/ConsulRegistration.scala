package com.servicediscovery.impl.consul

import com.ecwid.consul.v1.agent.model.NewService
import com.ecwid.consul.v1.{ConsulClient, QueryParams}


object ConsulRegistration extends App {

  serviceRegisterTest()

  @throws[InterruptedException]
  private def serviceRegisterTest(): Unit = {

    new Thread(() => {
      val consulClient = new ConsulClient("172.17.0.4", 8500)
      def foo() = {
        val waitTime = 1000
        val queryParams = new QueryParams(waitTime, 0)
        val test = consulClient.getCatalogService("test4", queryParams)
        if (test.getValue.isEmpty) {
          val consulIndex = test.getConsulIndex
          System.out.println("Test3 not registered. Current Index is " + consulIndex + " . Waiting for it to be registered")
          val test1 = consulClient.getCatalogService("test4", new QueryParams(waitTime, consulIndex))
          System.out.println("Got test3")
        }
        System.out.println(test)
        val catalogServices = consulClient.getCatalogServices(queryParams)
        System.out.println(catalogServices)
      }

      foo()
    }).start()


    Thread.sleep(5000)
    System.out.println("Registering test3 now")
    val newService = new NewService
    val serviceName = "test3"
    newService.setName(serviceName)
    val consulClient = new ConsulClient("127.0.0.1", 8500)
    consulClient.agentServiceRegister(newService)
    val agentServicesResponse = consulClient.getAgentServices
    val services = agentServicesResponse.getValue
    System.out.println(services)
    try
      Thread.sleep(10000000)
    catch {
      case e: InterruptedException ⇒
        e.printStackTrace()
    }
    //        assertThat(services, IsMapContaining.hasKey("test"));
  }
}
