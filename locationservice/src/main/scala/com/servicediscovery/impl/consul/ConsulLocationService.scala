package com.servicediscovery.impl.consul

import com.ecwid.consul.v1.ConsulClient

class ConsulLocationService {
  val consulClient = new ConsulClient("127.0.0.1", 8500)

}
