package models

import java.time.Instant

import io.waylay.influxdb.Influx.{IFloat, IPoint}
import play.api.libs.json.{Format, Json}

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

package object models {
  implicit val energyFormat: Format[Energy] = Json.format[Energy]
  implicit val thermostatFormat: Format[Thermostat] = Json.format[Thermostat]
}
