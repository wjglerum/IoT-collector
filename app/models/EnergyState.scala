package models

import play.api.libs.json.{Format, Json, JsonConfiguration, JsonNaming}

case class kWh(value: Double)

case class Watt(value: Int)

sealed trait Result

case class Energy(counter: String, counterToday: String, lastUpdate: String, usage: String) extends Result

case class DomoticzResult[+Result](result: Seq[Result])

object PascalCase extends JsonNaming {
  override val toString = "PascalCase"

  def apply(property: String): String =
    if (property.length > 0) {
      property.updated(0, Character.toUpperCase(property charAt 0))
    } else {
      property
    }
}

object Energy {
  implicit val config: JsonConfiguration.Aux[Json.MacroOptions] = JsonConfiguration(PascalCase)
  implicit val energyFormat: Format[Energy] = Json.format[Energy]
}

object DomoticzResult {
  implicit val energyStateFormat: Format[DomoticzResult[Energy]] = Json.format[DomoticzResult[Energy]]
}