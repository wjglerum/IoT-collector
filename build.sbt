name := """iot-collector"""
organization := "nl.wjglerum"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, DockerPlugin)

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  guice, ws,
  "io.waylay.influxdb" %% "influxdb-scala" % "2.0.1",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
)

dockerUpdateLatest := true
dockerUsername := Some("wjglerum")

mappings in Universal ~= (_.filterNot(_._1.name == "overrides.conf"))
