import java.time.Instant

import io.waylay.influxdb.Influx.{IFloat, IPoint}
import play.api.libs.json.{Format, Json}

import scala.language.implicitConversions

package object models {
  type Tag = (String, String)

  implicit val percentageDataPointFormat: Format[PercentageDataPoint] = Json.format[PercentageDataPoint]
  implicit val temperatureDataPointFormat: Format[TemperatureDataPoint] = Json.format[TemperatureDataPoint]
  implicit val activityDataPointsFormat: Format[ActivityDataPoints] = Json.format[ActivityDataPoints]
  implicit val sensorDataPointsFormat: Format[SensorDataPoints] = Json.format[SensorDataPoints]
  implicit val temperatureFormat: Format[Temperature] = Json.format[Temperature]
  implicit val settingFormat: Format[Setting] = Json.format[Setting]
  implicit val stateFormat: Format[State] = Json.format[State]
  implicit val weatherFormat: Format[Weather] = Json.format[Weather]

  implicit def stateToPoint(state: State)(implicit tags: Seq[Tag] = Nil): IPoint = IPoint(
    measurementName = "state",
    tags = tags,
    fields = Seq(
      "temperature" -> IFloat(state.sensorDataPoints.insideTemperature.celsius),
      "humidity" -> IFloat(state.sensorDataPoints.humidity.percentage),
      "heatingPower" -> IFloat(state.activityDataPoints.heatingPower.percentage),
      "setting" -> IFloat(state.setting.temperature.map(_.celsius).getOrElse(0))
    ),
    timestamp = Instant.parse(state.sensorDataPoints.insideTemperature.timestamp)
  )

  implicit def weatherToPoint(weather: Weather)(implicit tags: Seq[Tag] = Nil): IPoint = IPoint(
    measurementName = "weatherOutside",
    tags = tags,
    fields = Seq(
      "temperature" -> IFloat(weather.outsideTemperature.celsius),
      "solarIntensity" -> IFloat(weather.solarIntensity.percentage)
    ),
    timestamp = Instant.parse(weather.outsideTemperature.timestamp)
  )

  implicit def energyToPoint(energyState: Energy)(implicit tags: Seq[Tag] = Nil): IPoint = IPoint(
    measurementName = "utility",
    tags = tags,
    fields = Seq(
      "usage" -> IFloat(energyState.usage.replace(" Watt", "").toDouble),
      "counter" -> IFloat(energyState.counter.toDouble),
      "counterToday" -> IFloat(energyState.counterToday.replace(" kWh", "").toDouble)
    ),
    timestamp = Instant.parse(energyState.lastUpdate.replace(" ", "T") + ".00Z")
  )
}
