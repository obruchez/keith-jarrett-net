name := """keithjarrett-net"""
organization := "org.bruchez.olivier"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.15"

libraryDependencies ++= Seq(
  guice,
  "org.playframework" %% "play-slick" % "6.1.1",
  "org.playframework" %% "play-slick-evolutions" % "6.1.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
  "org.xerial" % "sqlite-jdbc" % "3.46.0.0"
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "org.bruchez.olivier.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "org.bruchez.olivier.binders._"
