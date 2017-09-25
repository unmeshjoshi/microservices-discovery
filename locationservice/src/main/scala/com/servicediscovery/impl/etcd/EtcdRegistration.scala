package com.servicediscovery.impl.etcd

import com.utils.Networks
import mousio.etcd4j.promises.EtcdResponsePromise
import mousio.etcd4j.responses.EtcdKeysResponse

object EtcdRegistration extends App {
  new EtcdRegistration().register
}

class EtcdRegistration {
  def register = {
    import java.net.URI

    import mousio.etcd4j.EtcdClient

    val eventualUnit1 = new Thread(() ⇒ {
      while (true) {
        val etcd = new EtcdClient(URI.create("http://192.168.0.111:4002"))
        println("waiting for value")
        var response: EtcdResponsePromise[EtcdKeysResponse] = etcd.get("/skydns/local/skydns/service/account/1").waitForChange().send()
        println(response.get().getNode.getKey)
        println(response.get().getNode.getValue)
        println(response.get().getAction)
      }
    }

    ).start()

    var i = 0;
    while (true) {
      val etcd = new EtcdClient(URI.create("http://192.168.0.111:4002"))
      val ip4Address = new Networks().ipv4Address.getHostAddress
      try {
        val send: EtcdResponsePromise[EtcdKeysResponse] = etcd.put("/skydns/local/skydns/service/account/1",
          s"""{ "host":"${ip4Address}", "port":8080, "priority": 20, "txt":"${i}"}""").send()
        send.get()
        println("registered value")
        i = i + 1
      }
      finally if (etcd != null) etcd.close()
      Thread.sleep(4000)
    }
  }

}
