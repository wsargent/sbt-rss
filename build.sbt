// this bit is important
sbtPlugin := true

organization := "com.typesafe.sbt"

name := "sbt-rss"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-deprecation", "-feature")

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  // RSS fetcher (note: the website is horribly outdated)
  "com.rometools" % "rome-fetcher" % "1.5.0"
)

publishMavenStyle := false

/** Console */
initialCommands in console := "import com.typesafe.sbt.rss._"
