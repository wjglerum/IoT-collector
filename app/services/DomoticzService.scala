package services

import java.text.SimpleDateFormat

import com.google.inject.Inject
import config.DomoticzConfig
import models.Energy
import play.api.Configuration
import play.api.http.Status.OK
import play.api.libs.json.{Json, JsonConfiguration, JsonNaming, Reads}
import play.api.libs.ws.WSClient
import services.DomoticzService.DomoticzResult

import scala.concurrent.{ExecutionContext, Future}

class DomoticzService @Inject()(configuration: Configuration,
                                ws: WSClient)(implicit ec: ExecutionContext) {

  private final val domoticzConfig: DomoticzConfig = configuration.get[DomoticzConfig]("domoticz")
  private final val domiticzUrl: String = s"http://${domoticzConfig.host}:${domoticzConfig.port}"
  private final val dateFormatter: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  def energy(id: Int): Future[Either[String, Energy]] = {
    ws.url(s"$domiticzUrl/json.htm?type=devices&rid=$id").get.map { response =>
      response.status match {
        case OK => response.json.as[DomoticzResult].result.headOption.map { result =>
          Right(
            Energy(
              timestamp = dateFormatter.parse(result.lastUpdate).toInstant,
              usage = result.usage.replace("Watt", "").toDouble,
              counterToday = result.counterToday.replace("kWh", "").toDouble,
              counter = result.counter.toDouble
            )
          )
        }.getOrElse(Left(s"No measurement found for sensor id $id"))
        case _ => Left(response.body)
      }
    }.recover {
      case t => Left(t.getMessage)
    }
  }
}

object DomoticzService {

  case class DomoticzResult(result: Seq[EnergyResult])

  case class EnergyResult(counter: String, counterToday: String, lastUpdate: String, usage: String)

  object EnergyResult {

    object PascalCase extends JsonNaming {
      override val toString = "PascalCase"

      def apply(property: String): String =
        if (property.length > 0)
          property.updated(0, property.head.toUpper)
        else property
    }

    implicit val config: JsonConfiguration.Aux[Json.MacroOptions] = JsonConfiguration(PascalCase)
    implicit val energyResultReads: Reads[EnergyResult] = Json.reads[EnergyResult]
  }

  implicit val domoticzResultReads: Reads[DomoticzResult] = Json.reads[DomoticzResult]
}
