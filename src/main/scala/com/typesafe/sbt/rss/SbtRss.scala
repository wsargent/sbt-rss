package com.typesafe.sbt.rss

import sbt._
import Keys._
import sbt.complete.Parsers._

import java.net.URL
import com.rometools.fetcher._
import com.rometools.fetcher.impl._
import com.rometools.rome.feed.synd._

import scala.util.control.NonFatal

/**
 * An autoplugin that displays an RSS feed.
 */
object SbtRss extends AutoPlugin {

  object autoImport {
    val rssList = settingKey[Seq[String]]("The list of RSS urls to update.")

//    lazy val baseRssSettings: Seq[sbt.Def.Setting[_]] = Seq(
//      rssList := "http://localhost:9000/atom"
//    )
  }

  import autoImport._

  override def globalSettings: Seq[Setting[_]] = super.globalSettings ++ Seq(
    Keys.commands += rssCommand
  )

  //override def projectSettings: Seq[Setting[_]] = baseRssSettings

  private val args = (Space ~> StringBasic).*

  private lazy val rssCommand = Command("rss")(_ => args)(doCommand)

  private val feedInfoCache = HashMapFeedInfoCache.getInstance()

  private val fetcher = new HttpURLFeedFetcher(feedInfoCache)

  class FetcherEventListenerImpl(state:State) extends FetcherListener {
   def fetcherEvent(event:FetcherEvent) = {
     import FetcherEvent._
      event.getEventType() match {
        case EVENT_TYPE_FEED_POLLED =>
          state.log.debug("\tEVENT: Feed Polled. URL = " + event.getUrlString())
        case EVENT_TYPE_FEED_RETRIEVED =>
          state.log.debug("\tEVENT: Feed Retrieved. URL = " + event.getUrlString())
        case EVENT_TYPE_FEED_UNCHANGED =>
          state.log.debug("\tEVENT: Feed Unchanged. URL = " + event.getUrlString())
      }
    }
  }

  def doCommand(state: State, args: Seq[String]): State = {
    import scala.collection.JavaConverters._

    val extracted = Project.extract(state)
    import extracted._

    val listener = new FetcherEventListenerImpl(state)
    fetcher.addFetcherEventListener(listener)

    try {
      val currentList = (rssList in currentRef get structure.data).get
      for (currentUrl <- currentList) {
        val feedUrl = new URL(currentUrl)

        val feed = fetcher.retrieveFeed(feedUrl)
        val title = feed.getTitle.trim()
        val publishDate = feed.getPublishedDate
        val entries = feed.getEntries().asScala
        val firstEntry = entries.head
        
        state.log.info(s"Showing $feedUrl")
        state.log.info(s"\t\tTitle = $title")
        state.log.info(s"\t\tPublished = $publishDate")
        state.log.info(s"\t\tMost recent entry = ${firstEntry.getTitle.trim()}")
        state.log.info(s"\t\tEntry updated = " + firstEntry.getUpdatedDate)
      }
    } catch {
      case NonFatal(e) =>
        state.log.error(s"Error ${e.getMessage}")
    } finally {
      fetcher.removeFetcherEventListener(listener)
    }

    state
  }

}
