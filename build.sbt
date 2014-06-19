sbtPlugin := true

organization := "com.typesafe.sbt"

name := "sbt-rss"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-deprecation", "-feature")

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.2"
)

publishMavenStyle := false

/** Console */
initialCommands in console := "import com.typesafe.sbt.rss._"

testOptions := Seq(Tests.Filter(s =>
  Seq("Spec", "Suite", "Unit", "all").exists(s.endsWith(_)) &&
    !s.endsWith("FeaturesSpec") ||
    s.contains("UserGuide") ||
    s.contains("index") ||
    s.matches("org.specs2.guide.*")))


// use withActorSystem from sbt-web

// use akka-http client from Spray to talk to remote server

// use webdriver.
