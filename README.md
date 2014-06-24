# SBT RSS

This is an SBT 0.13 plugin which uses AutoPlugin and the ROME libraries to 
query websites for RSS feeds and display the titles inside SBT.

This is a fairly simple SBT plugin, and has lots of comments to show how you can add a command to SBT.

## Building 

You can build and publish the plugin in the normal way to your local Ivy repository:

```
sbt publish-local
```

## Installation

You must first download the git project and build it.  It is not available in the maven repository.

In `project/plugins.sbt`:

```
addSbtPlugin("com.typesafe.sbt" % "sbt-rss" % "1.0.0-SNAPSHOT")
```

In `build.sbt`:

```
val myProject = (project in file(".")).enablePlugins(SbtRss)

rssList := Seq(
  "http://typesafe.com/blog/rss.xml",
  "http://letitcrash.com/rss",
  "https://github.com/akka/akka.github.com/commits/master/news/_posts.atom"
)
```

## Usage

Once you have it installed, typing `rss` at an SBT prompt will show latest updates.

```
> rss
[info] Showing http://typesafe.com/blog/rss.xml
[info] 		Title = The Typesafe Blog
[info] 		Published = null
[info] 		Most recent entry = Scala Days Presentation Roundup
[info] 		Entry updated = null
[info] Showing http://letitcrash.com/rss
[info] 		Title = Let it crash
[info] 		Published = null
[info] 		Most recent entry = Reactive Queue with Akka Reactive Streams
[info] 		Entry updated = null
[info] Showing https://github.com/akka/akka.github.com/commits/master/news/_posts.atom
[info] 		Title = Recent Commits to akka.github.com:master
[info] 		Published = Thu May 22 05:51:21 EDT 2014
[info] 		Most recent entry = Fix fixed issue list.
[info] 		Entry updated = Thu May 22 05:51:21 EDT 2014
```

Or, you can provide URLs on the command line. 

```
> rss http://tersesystems.com/atom.xml
[info] Showing http://tersesystems.com/atom.xml
[info] 		Title = Terse Systems
[info] 		Published = Thu May 29 03:40:53 EDT 2014
[info] 		Most recent entry = Testing Hostname Verification
[info] 		Entry updated = Mon Mar 31 21:35:00 EDT 2014
```

## Testing

Go to the `sbt-rss-tester` project in the github project and type `rss`.
 
```
cd sbt-rss-tester
sbt
rss
```
 
Type `rss` at an SBT prompt.


