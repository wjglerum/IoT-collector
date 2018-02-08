package models

import play.api.libs.json.{Format, Json}

case class State(activityDataPoints: ActivityDataPoints,
                 sensorDataPoints: SensorDataPoints)

case class ActivityDataPoints(heatingPower: PercentageDataPoint)
case class SensorDataPoints(humidity: PercentageDataPoint, insideTemperature: TemperatureDataPoint)

sealed trait DataPoint
case class PercentageDataPoint(percentage: Double, timestamp: String) extends DataPoint
case class TemperatureDataPoint(celsius: Double, timestamp: String) extends DataPoint

case class Weather(outsideTemperature: TemperatureDataPoint, solarIntensity: PercentageDataPoint)
