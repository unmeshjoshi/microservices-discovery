package com.customer.ui

import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.{SelectionKey, Selector, ServerSocketChannel, SocketChannel}
import java.util

object SimpleNIOServer {
  @SuppressWarnings(Array("unused"))
  @throws[IOException]
  def main(args: Array[String]): Unit = { // Selector: multiplexor of SelectableChannel objects
    val selector = Selector.open
    // selector is open here
    // ServerSocketChannel: selectable channel for stream-oriented listening sockets
    val simpleSocket = ServerSocketChannel.open
    val simpleAddr = new InetSocketAddress("localhost", 1111)
    // Binds the channel's socket to a local address and configures the socket to listen for connections
    simpleSocket.bind(simpleAddr)
    // Adjusts this channel's blocking mode.
    simpleSocket.configureBlocking(false)
    val ops = simpleSocket.validOps
    val selectKy = simpleSocket.register(selector, ops, null)
    // Infinite loop..
    // Keep server running
    while ( {
      true
    }) {
      log("i'm a server and i'm waiting for new connection and buffer select...")
      // Selects a set of keys whose corresponding channels are ready for I/O operations
      selector.select
      // token representing the registration of a SelectableChannel with a Selector
      val simpleKeys = selector.selectedKeys
      val simpleIterator = simpleKeys.iterator
      while ( {
        simpleIterator.hasNext
      }) {
        handleKey(selector, simpleSocket, simpleIterator.next)
        simpleIterator.remove
      }
    }
  }

  private def handleKey(selector: Selector, simpleSocket: ServerSocketChannel, key: SelectionKey) = {
    // Tests whether this key's channel is ready to accept a new socket connection
    if (key.isAcceptable) {
      acceptConnection(selector, simpleSocket)
    }
    else if (key.isReadable) {
      readFromConnection(selector, key)

    } else if (key.isWritable) {
      writeToConnection(key)
    }
  }

  private def writeToConnection(myKey: SelectionKey) = {
    val simpleClient = myKey.channel.asInstanceOf[SocketChannel]
    println(s"${simpleClient} is writable")
    val simpleBuffer = ByteBuffer.wrap("Hello".getBytes)
    simpleClient.write(simpleBuffer)
  }

  private def readFromConnection(selector: Selector, myKey: SelectionKey) = {
    val simpleClient = myKey.channel.asInstanceOf[SocketChannel]
    println(s"${simpleClient} is readable")

    simpleClient.configureBlocking(false)
    val simpleBuffer = ByteBuffer.allocate(256)
    simpleClient.read(simpleBuffer)
    val result = new String(simpleBuffer.array).trim
    log("Message received: " + result)
    if (result == "simple") {
      simpleClient.close
      log("\nIt's time to close connection as we got last company name 'simple'")
      log("\nServer will keep running. Try running client again to establish new connection")
    }
    simpleClient.register(selector, SelectionKey.OP_WRITE)
  }

  private def acceptConnection(selector: Selector, simpleSocket: ServerSocketChannel) = {
    val simpleClient = simpleSocket.accept
    // Adjusts this channel's blocking mode to false
    simpleClient.configureBlocking(false)
    // Operation-set bit for read operations
    simpleClient.register(selector, SelectionKey.OP_READ)
    log("Connection Accepted: " + simpleClient.getLocalAddress + "\n")
  }

  private def log(str: String) = {
    System.out.println(str)
  }
}