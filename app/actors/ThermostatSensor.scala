package actors

import actors.Sensor.{Error, PollByID, PollKey, ReadingByID}
import actors.StorageActor.StoreWithTags
import akka.actor.ActorRef
import akka.pattern.pipe
import com.google.inject.Inject
import com.google.inject.name.Named
import config.TadoConfig
import play.api.{Configuration, Logger}
import services.TadoService

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

class ThermostatSensor @Inject()(configuration: Configuration,
                                 tadoService: TadoService,
                                 @Named("storageActor") storageActor: ActorRef)
                                (implicit ec: ExecutionContext) extends Sensor {

  private val logger: Logger = Logger(this.getClass)
  private val tadoConfig = configuration.get[TadoConfig]("tado")

  override def preStart(): Unit = tadoConfig.zones.foreach { id =>
    timers.startTimerWithFixedDelay(PollKey(id), PollByID(id), 1 minute)
  }

  override def receive: Receive = {
    case PollByID(id) =>
      logger.info(s"Polling thermostat with id $id")
      tadoService.thermostat(id).map {
        case Left(message) => Error(message)
        case Right(thermostat) =>
          val tags = Seq("device" -> "tado", "zone" -> id.toString)
          StoreWithTags(thermostat, tags)
      } pipeTo storageActor
    case ReadingByID(id) =>
      logger.info(s"Reading thermostat with id $id")
      tadoService.thermostat(id).map {
        case Left(message) => Error(message)
        case Right(measurement) => measurement
      } pipeTo sender
  }
}
