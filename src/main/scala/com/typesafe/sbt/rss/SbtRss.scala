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

  /**
   * Sets up the autoimports of setting keys.
   */
  object autoImport {
    /**
     * Defines "rssList" as the setting key that we want the user to fill out.
     */
    val rssList = settingKey[Seq[String]]("The list of RSS urls to update.")
  }

  // I don't know why we do this.
  import autoImport._

  /**
   * An internal cache to avoid hitting RSS feeds repeatedly.
   */
  private val feedInfoCache = HashMapFeedInfoCache.getInstance()

  /**
   * An RSS fetcher, backed by the cache.
   */
  private val fetcher = new HttpURLFeedFetcher(feedInfoCache)

  /** Allows the RSS command to take string arguments. */
  private val args = (Space ~> StringBasic).*

  /** The RSS command, mapped into sbt as "rss [args]" */
  private lazy val rssCommand = Command("rss")(_ => args)(doRssCommand)

  /**
   * Adds the rssCommand to the list of global commands in SBT.
   */
  override def globalSettings: Seq[Setting[_]] = super.globalSettings ++ Seq(
    Keys.commands += rssCommand
  )

  /**
   * The actual RSS command.
   *
   * @param state the state of the RSS application.
   * @param args the string arguments provided to "rss"
   * @return the unchanged state.
   */
  def doRssCommand(state: State, args: Seq[String]): State = {
    state.log.debug(s"args = $args")

    // Doing Project.extract(state) and then importing it gives us currentRef.
    // Using currentRef allows us to get at the values of SettingKey.
    // http://www.scala-sbt.org/release/docs/Build-State.html#Project-related+data
    val extracted = Project.extract(state)
    import extracted._

    // Create a new fetcher event listener attached to the state -- this gives
    // us a way to log the fetcher events.
    val listener = new FetcherEventListenerImpl(state)
    fetcher.addFetcherEventListener(listener)

    try {
      if (args.isEmpty) {
        // This is the way we get the setting from rssList := Seq("http://foo.com/rss")
        // http://www.scala-sbt.org/release/docs/Build-State.html#Project+data
        val currentList = (rssList in currentRef get structure.data).get
        for (currentUrl <- currentList) {
          val feedUrl = new URL(currentUrl)
          printFeed(feedUrl, state)
        }
      } else {
        for (currentUrl <- args) {
          val feedUrl = new URL(currentUrl)
          printFeed(feedUrl, state)
        }
      }
    } catch {
      case NonFatal(e) =>
        state.log.error(s"Error ${e.getMessage}")
    } finally {
      // Remove the listener so we don't have a memory leak.
      fetcher.removeFetcherEventListener(listener)
    }

    state
  }

  def printFeed(feedUrl:URL, state:State) = {
    // Allows us to do "asScala" conversion from java.util collections.
    import scala.collection.JavaConverters._

    // This is a blocking operation, but we're in SBT, so we don't care.
    val feed = fetcher.retrieveFeed(feedUrl)
    val title = feed.getTitle.trim()
    val publishDate = feed.getPublishedDate
    val entries = feed.getEntries.asScala
    val firstEntry = entries.head

    // The only way to provide the RSS feeds as a resource seems to be to
    // have another plugin extend this one.  The code's small enough that it
    // doesn't seem worth it.
    state.log.info(s"Showing $feedUrl")
    state.log.info(s"\t\tTitle = $title")
    state.log.info(s"\t\tPublished = $publishDate")
    state.log.info(s"\t\tMost recent entry = ${firstEntry.getTitle.trim()}")
    state.log.info(s"\t\tEntry updated = " + firstEntry.getUpdatedDate)
  }

  /**
   * Listens for RSS events.
   *
   * @param state
   */
  class FetcherEventListenerImpl(state:State) extends FetcherListener {
    def fetcherEvent(event:FetcherEvent) = {
      import FetcherEvent._
      event.getEventType match {
        case EVENT_TYPE_FEED_POLLED =>
          state.log.debug("\tEVENT: Feed Polled. URL = " + event.getUrlString)
        case EVENT_TYPE_FEED_RETRIEVED =>
          state.log.debug("\tEVENT: Feed Retrieved. URL = " + event.getUrlString)
        case EVENT_TYPE_FEED_UNCHANGED =>
          state.log.debug("\tEVENT: Feed Unchanged. URL = " + event.getUrlString)
      }
    }
  }

}
