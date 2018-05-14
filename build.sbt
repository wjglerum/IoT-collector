name := """iot-collector"""
organization := "nl.wjglerum"

version := "1.0-SCALA-DAYS-2018"

lazy val root = (project in file(".")).enablePlugins(PlayScala, DockerPlugin)

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  guice, ws,
  "io.waylay.influxdb" %% "influxdb-scala" % "2.0.1",
  "com.lightbend.akka" %% "akka-stream-alpakka-mqtt" % "0.18",
"org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
)

//dockerUpdateLatest := true
dockerUsername := Some("wjglerum")
dockerBaseImage := "arm64v8/openjdk"

mappings in Universal ~= (_.filterNot(_._1.name == "overrides.conf"))
