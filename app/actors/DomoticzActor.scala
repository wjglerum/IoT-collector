package actors

import akka.actor.Actor
import com.google.inject.Inject
import play.api.Logger
import services.{DomoticzService, InfluxDBService}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class DomoticzActor @Inject()(domoticzService: DomoticzService,
                              influxDBService: InfluxDBService)(implicit ec: ExecutionContext) extends Actor {

  def receive = {
    case Poll(id) =>
      Logger.info("--- Polling domoticz ---")
      domoticzService.utility(id).flatMap {
        case Left(error) => Future.successful(Logger.error(error))
        case Right(measurement) =>
          implicit val tags: Seq[(String, String)] = Seq(
            "device" -> "domoticz",
            "deviceId" -> id.toString
          )
          influxDBService.store("domoticz", Seq(measurement))
      }
  }
}
