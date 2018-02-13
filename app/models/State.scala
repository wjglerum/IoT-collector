package models

case class State(activityDataPoints: ActivityDataPoints,
                 sensorDataPoints: SensorDataPoints,
                 setting: Setting)

case class Setting(temperature: Option[Temperature])

case class Temperature(celsius: Double)

case class ActivityDataPoints(heatingPower: PercentageDataPoint)

case class SensorDataPoints(humidity: PercentageDataPoint, insideTemperature: TemperatureDataPoint)

sealed trait DataPoint

case class PercentageDataPoint(percentage: Double, timestamp: String) extends DataPoint

case class TemperatureDataPoint(celsius: Double, timestamp: String) extends DataPoint

case class Weather(outsideTemperature: TemperatureDataPoint, solarIntensity: PercentageDataPoint)
