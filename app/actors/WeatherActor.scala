package actors

import akka.actor.Actor
import com.google.inject.Inject
import play.api.Logger
import services.{InfluxDBService, TadoService}

import scala.concurrent.{ExecutionContext, Future}

class WeatherActor @Inject()(tadoAPI: TadoService,
                             influxDBService: InfluxDBService)(implicit ec: ExecutionContext) extends Actor {

  def receive = {
    case Poll =>
      Logger.info("--- Polling weather ---")
      tadoAPI.weather.flatMap {
        case Left(error) => Future.successful(Logger.error(error))
        case Right(measurement) =>
          implicit val tags: Seq[(String, String)] = Seq(
            "device" -> "tado"
          )
          influxDBService.store("weather", Seq(measurement))
      }
  }
}
