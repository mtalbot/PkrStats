name := "PkrLog"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "com.typesafe.slick" %% "slick" % "2.0.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.5",
  "com.github.tototoshi" %% "slick-joda-mapper" % "1.1.0",
  "com.typesafe.akka" %% "akka-actor" % "2.3.2",
  "com.typesafe.play" %% "play-slick" % "0.6.0.1"
)     

play.Project.playScalaSettings
