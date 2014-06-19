name := "PkrLog"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  filters,
  "com.typesafe.slick" %% "slick" % "2.0.2",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.5",
  "com.github.tototoshi" %% "slick-joda-mapper" % "1.1.0",
  "com.typesafe.akka" %% "akka-actor" % "2.2.2",
  "com.typesafe.play" %% "play-slick" % "0.6.0.1",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.4.0-rc2",
  "com.google.api-client" % "google-api-client" % "1.18.0-rc",
  "com.google.http-client" % "google-http-client-jackson2" % "1.18.0-rc",
  "com.google.http-client" % "google-http-client" % "1.18.0-rc",
  "com.google.oauth-client" % "google-oauth-client-jetty" % "1.18.0-rc",
  "com.google.oauth-client" % "google-oauth-client" % "1.18.0-rc",
  "com.google.oauth-client" % "google-oauth-client-java6" % "1.18.0-rc",
  "org.mortbay.jetty" % "jetty" % "6.1.26",
  "com.google.apis" % "google-api-services-plus" % "v1-rev126-1.18.0-rc"
)     

play.Project.playScalaSettings

routesImport ++= Seq("components.LongBinder._")

pipelineStages := Seq(rjs)
