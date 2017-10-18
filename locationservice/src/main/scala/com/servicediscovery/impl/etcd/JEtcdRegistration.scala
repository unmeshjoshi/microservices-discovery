package com.servicediscovery.impl.etcd

import java.net.InetAddress

import com.coreos.jetcd.Client
import com.coreos.jetcd.data.ByteSequence
import com.utils.Networks

object JEtcdRegistration extends App {
  new JEtcdRegistration().register
}

class JEtcdRegistration {


  val hostIp: InetAddress = new Networks().ipv4Address //get ip address of primary interface.
  def url(hostIp:InetAddress, port:Int) = s"http:/${hostIp}:${port}"
  println(s"host ip address is ${hostIp}")

  def register = {

    val eventualUnit1 = new Thread(() ⇒ {
      val etcdClient = Client.builder.endpoints(url(hostIp, 4002), url(hostIp, 4001)).build

      while (true) {
        val watchClient = etcdClient.getWatchClient
        import com.coreos.jetcd.data.ByteSequence
        val watcher = watchClient.watch(ByteSequence.fromString("/skydns/local/skydns/service/account/1"))
        println("waiting for value")
        val response = watcher.listen()
        println(s"got response ${response}")
        response.getEvents.forEach(action ⇒ {
          println(new String(action.getKeyValue.getValue.getBytes))
        })
      }
    }

    ).start()

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
