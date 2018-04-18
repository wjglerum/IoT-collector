package models

import java.time.Instant

import io.waylay.influxdb.Influx.{IFloat, IPoint}

sealed trait Measurement {
  type Tag = (String, String)
  val timestamp: Instant

  def toPoint: IPoint = toPointWithTags(Nil)

  def toPointWithTags(tags: Seq[Tag]): IPoint
}

case class Energy(timestamp: Instant, usage: Double, counterToday: Double, counter: Double) extends Measurement {
  override def toPointWithTags(tags: Seq[Tag]): IPoint = IPoint(
    measurementName = "energy",
    tags = tags,
    fields = Seq(
      "usage" -> IFloat(usage),
      "counterToday" -> IFloat(counterToday),
      "counter" -> IFloat(counter)
    ),
    timestamp = Instant.now
  )
}

case class Thermostat(timestamp: Instant, temperature: Double, humidity: Double, heatingPower: Double) extends Measurement {
  override def toPointWithTags(tags: Seq[(String, String)]): IPoint = IPoint(
    measurementName = "thermostat",
    tags = tags,
    fields = Seq(
      "temperature" -> IFloat(temperature),
      "humidity" -> IFloat(humidity),
      "heatingPower" -> IFloat(heatingPower)
    ),
    timestamp = timestamp
  )
}

case class OutsideWeather(timestamp: Instant, temperature: Double, solarIntensity: Double) extends Measurement {
  override def toPointWithTags(tags: Seq[(String, String)]): IPoint = IPoint(
    measurementName = "weather_outside",
    tags = tags,
    fields = Seq(
      "temperature" -> IFloat(temperature),
      "solarIntensity" -> IFloat(solarIntensity)
    ),
    timestamp = timestamp
  )
}
