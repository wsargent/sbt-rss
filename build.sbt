sbtPlugin := true

organization := "com.typesafe.sbt"

name := "sbt-rss"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-deprecation", "-feature")

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "com.rometools" % "rome-fetcher" % "1.5.0"
)

publishMavenStyle := false

/** Console */
initialCommands in console := "import com.typesafe.sbt.rss._"

// use withActorSystem from sbt-web

// use akka-http client from Spray to talk to remote server

// use webdriver.
