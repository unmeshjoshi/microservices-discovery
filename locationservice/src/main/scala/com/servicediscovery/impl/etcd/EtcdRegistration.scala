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
      val etcd = new EtcdClient(URI.create("http://172.17.0.2:4001"))
      println("waiting for value")
      val change = etcd.get("/skydns/local/skydns/service/account/1").waitForChange().send()
      println(change.get().getNode.getKey)
      println(change.get().getNode.getValue)
      println(change.get().getAction)
  }

  ).start()

  var i = 0;
  while (true) {
    val etcd = new EtcdClient(URI.create("http://172.17.0.2:4001"))
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
