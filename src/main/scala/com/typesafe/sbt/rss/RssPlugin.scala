package com.typesafe.sbt.rss

import sbt._
import Keys._
import akka.actor.{ActorSystem, ActorRefFactory}
import sbt.complete.Parsers._

object Import {

  object RssKeys {
    val url = SettingKey[String]("rssUrl", "The URL that contains RSS.")
  }

}

/**
 *
 */
object SbtRss extends AutoPlugin {

  val autoImport = Import

  import autoImport._

  override def globalSettings: Seq[Setting[_]] = super.globalSettings ++ Seq(
    onLoad in Global := (onLoad in Global).value andThen load,
    onUnload in Global := (onUnload in Global).value andThen unload,
    Keys.commands += rssCommand
  )

  private val args = (Space ~> StringBasic).*

  private lazy val rssCommand = Command("rss")(_ => args)(doCommand)

  def doCommand(state: State, args: Seq[String]): State = {
    state.log.info("Hello world!")

    state
  }

  // Actor system management and API

  private val rssActorSystemAttrKey = AttributeKey[ActorSystem]("rss-actor-system")

  private def load(state: State): State = {
    state.get(rssActorSystemAttrKey).fold({
      val rssActorSystem = withActorClassloader(ActorSystem("sbt-rss"))
      state.put(rssActorSystemAttrKey, rssActorSystem)
    })(as => state)
  }

  private def unload(state: State): State = {
    state.get(rssActorSystemAttrKey).fold(state) {
      as =>
        as.shutdown()
        state.remove(rssActorSystemAttrKey)
    }
  }

  /**
   * Perform actor related activity with sbt-web's actor system.
   * @param state The project build state available to the task invoking this.
   * @param namespace A means by which actors can be namespaced.
   * @param block The block of code to execute.
   * @tparam T The expected return type of the block.
   * @return The return value of the block.
   */
  def withActorRefFactory[T](state: State, namespace: String)(block: (ActorRefFactory) => T): T = {
    // We will get an exception if there is no known actor system - which is a good thing because
    // there absolutely has to be at this point.
    block(state.get(rssActorSystemAttrKey).get)
  }

  private def withActorClassloader[A](f: => A): A = {
    val newLoader = ActorSystem.getClass.getClassLoader
    val thread = Thread.currentThread
    val oldLoader = thread.getContextClassLoader

    thread.setContextClassLoader(newLoader)
    try {
      f
    } finally {
      thread.setContextClassLoader(oldLoader)
    }
  }

}
