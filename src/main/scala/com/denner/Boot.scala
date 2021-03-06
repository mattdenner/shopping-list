package com.denner

import com.typesafe.config.ConfigFactory
import akka.actor.{Props, ActorSystem}
import spray.can.server.HttpServer
import spray.io._

object Boot extends App {
  val config = ConfigFactory.load()
  val system = ActorSystem("ShoppingList", config.getConfig("shopping-list").withFallback(config))

  val ioBridge = new IOBridge(system).start()
  val service  = system.actorOf(Props[Main], "main")

  val httpServer = system.actorOf(
    Props(new HttpServer(ioBridge, SingletonHandler(service))),
    name = "http-server"
  )

  httpServer ! HttpServer.Bind("0.0.0.0", util.Properties.envOrElse("PORT", "8080").toInt)

  system.registerOnTermination {
    ioBridge.stop()
  }
}
