val root = (project in file(".")).enablePlugins(SbtRss)

rssList := Seq(
  "http://typesafe.com/blog/rss.xml",
  "http://letitcrash.com/rss",
  "https://github.com/akka/akka.github.com/commits/master/news/_posts.atom"
)
