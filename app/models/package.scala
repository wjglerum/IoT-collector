import play.api.libs.json.{Format, Json}

package object models {
  implicit val percentageDataPointFormat: Format[PercentageDataPoint] = Json.format[PercentageDataPoint]
  implicit val temperatureDataPointFormat: Format[TemperatureDataPoint] = Json.format[TemperatureDataPoint]
  implicit val activityDataPointsFormat: Format[ActivityDataPoints] = Json.format[ActivityDataPoints]
  implicit val sensorDataPointsFormat: Format[SensorDataPoints] = Json.format[SensorDataPoints]
  implicit val stateFormat: Format[State] = Json.format[State]
  implicit val weatherFormat: Format[Weather] = Json.format[Weather]
}
