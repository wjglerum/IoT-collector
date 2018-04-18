import play.api.libs.json.{Format, Json}

package object models {
  implicit val energyFormat: Format[Energy] = Json.format[Energy]
  implicit val thermostatFormat: Format[Thermostat] = Json.format[Thermostat]
  implicit val outsideWeatherFormat: Format[OutsideWeather] = Json.format[OutsideWeather]
}
