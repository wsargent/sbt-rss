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
    val rss = inputKey[Unit]("Prints RSS")
  }

  // If you change your auto import then the change is automatically reflected here..
  import autoImport._

  /**
   * An internal cache to avoid hitting RSS feeds repeatedly.
   *
   */
  private val feedInfoCache = HashMapFeedInfoCache.getInstance()

  /**
   * An RSS fetcher, backed by the cache.
   */
  private val fetcher = new HttpURLFeedFetcher(feedInfoCache)

  /** Allows the RSS command to take string arguments. */
  private val argsParser = (Space ~> StringBasic).*

  
  /**
   * Adds the rss task to the list of tasks for the project.
   */
  override def projectSettings: Seq[Setting[_]] = Seq(
    rssSetting
  )

  /**
   * The actual RSS command.
   *
   * @param state the state of the RSS application.
   * @param args the string arguments provided to "rss"
   * @return the unchanged state.
   */
  def rssSetting: Setting[_] = rss := {
    // Parse the input string into space-delimited strings.
    val args = argsParser.parsed
    // Sbt provided logger.
    val log = streams.value.log
    log.debug(s"args = $args")

    // Create a new fetcher event listener attached to the state -- this gives
    // us a way to log the fetcher events.
    val listener = new FetcherEventListenerImpl(log)
    fetcher.addFetcherEventListener(listener)

    try {
      if (args.isEmpty) {
        // This is the way we get the setting from rssList := Seq("http://foo.com/rss")
        // The .? means that the setting may or may not exist.  If it doesn't,
        //   for now we return an empty sequence.
        // The .value means the dependency is computed *asynchronously* before this function 
        //   is run, and we get the resulting value
        val currentList = rssList.?.value.getOrElse(Nil)
        for (currentUrl <- currentList) {
          val feedUrl = new URL(currentUrl)
          printFeed(feedUrl, log)
        }
      } else {
        for (currentUrl <- args) {
          val feedUrl = new URL(currentUrl)
          printFeed(feedUrl, log)
        }
      }
    } catch {
      case NonFatal(e) =>
        log.error(s"Error ${e.getMessage}")
    } finally {
      // Remove the listener so we don't have a memory leak.
      fetcher.removeFetcherEventListener(listener)
    }
    // We return nothing, or unit.
    ()
  }

  def printFeed(feedUrl:URL, log: Logger) = {
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
    log.info(s"Showing $feedUrl")
    log.info(s"\t\tTitle = $title")
    log.info(s"\t\tPublished = $publishDate")
    log.info(s"\t\tMost recent entry = ${firstEntry.getTitle.trim()}")
    log.info(s"\t\tEntry updated = " + firstEntry.getUpdatedDate)
  }

  /**
   * Listens for RSS events.
   *
   * @param state
   */
  class FetcherEventListenerImpl(log: Logger) extends FetcherListener {
    def fetcherEvent(event:FetcherEvent) = {
      import FetcherEvent._
      event.getEventType match {
        case EVENT_TYPE_FEED_POLLED =>
          log.debug("\tEVENT: Feed Polled. URL = " + event.getUrlString)
        case EVENT_TYPE_FEED_RETRIEVED =>
          log.debug("\tEVENT: Feed Retrieved. URL = " + event.getUrlString)
        case EVENT_TYPE_FEED_UNCHANGED =>
          log.debug("\tEVENT: Feed Unchanged. URL = " + event.getUrlString)
      }
    }
  }

}
