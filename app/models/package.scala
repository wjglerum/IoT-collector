import play.api.libs.json.{Format, Json}

package object models {
  implicit val RPIFormat: Format[RPIReading] = Json.format[RPIReading]
  implicit val MessageFormat: Format[Message] = Json.format[Message]
}
