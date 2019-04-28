package actors

import actors.Sensor._
import actors.StorageActor.StoreWithTags
import akka.actor.ActorRef
import akka.pattern.pipe
import com.google.inject.Inject
import com.google.inject.name.Named
import play.api.{Configuration, Logger}
import services.TadoService

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

class WeatherSensor @Inject()(configuration: Configuration,
                              tadoService: TadoService,
                              @Named("storageActor") storageActor: ActorRef)
                             (implicit ec: ExecutionContext) extends Sensor {

  private val logger: Logger = Logger(this.getClass)

  override def preStart(): Unit = timers.startPeriodicTimer(PollKey, Poll, 5 minutes)

  override def receive: Receive = {
    case Poll =>
      logger.info(s"Polling weather")
      tadoService.weather.map {
        case Left(message) => Error(message)
        case Right(weather) =>
          val tags = Seq("device" -> "tado")
          StoreWithTags(weather, tags)
      } pipeTo storageActor
    case Reading =>
      logger.info(s"Reading weather")
      tadoService.weather.map {
        case Left(message) => Error(message)
        case Right(measurement) => measurement
      } pipeTo sender
  }
}
