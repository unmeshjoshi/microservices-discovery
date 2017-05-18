package servicediscovery

import org.apache.zookeeper.{WatchedEvent, Watcher, ZooKeeper}

object Master {
  @throws[Exception]
  def main(args: Array[String]): Unit = {
    val m = new Master("127.0.0.1:2181")
    m.startZK()
    // wait for a bit
    Thread.sleep(60000)
  }
}

class Master(var hostPort: String) extends Watcher {
  var zk:ZooKeeper = _

  def startZK(): Unit = {
    zk = new ZooKeeper(hostPort, 15000, this)
  }

  override def process(e: WatchedEvent): Unit = {
    System.out.println(e)
  }
}