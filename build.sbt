name := """iot-collector"""
organization := "nl.wjglerum"

version := "1.2"

lazy val root = (project in file(".")).enablePlugins(PlayScala, DockerPlugin)

scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  guice, ws,
  "io.waylay.influxdb" %% "influxdb-scala" % "3.0.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
)

dockerUpdateLatest := true
dockerUsername := Some("wjglerum")

mappings in Universal ~= (_.filterNot(_._1.name == "overrides.conf"))
