name := """tado-api"""
organization := "nl.wjglerum"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, DockerPlugin)

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  guice, ws,
  "io.waylay.influxdb" %% "influxdb-scala" % "2.0.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
)
