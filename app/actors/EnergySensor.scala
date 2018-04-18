package actors

import actors.Sensor._
import actors.StorageActor.StoreWithTags
import akka.actor.ActorRef
import akka.pattern.pipe
import com.google.inject.Inject
import com.google.inject.name.Named
import config.DomoticzConfig
import play.api.{Configuration, Logger}
import services.DomoticzService

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

class EnergySensor @Inject()(configuration: Configuration,
                             domoticzService: DomoticzService,
                             @Named("storageActor") storageActor: ActorRef)
                            (implicit ec: ExecutionContext) extends Sensor {

  val domoticzConfig: DomoticzConfig = configuration.get[DomoticzConfig]("domoticz")

  override def preStart(): Unit = domoticzConfig.sensors.foreach { id =>
    timers.startPeriodicTimer(PollKey, PollByID(id), 10 seconds)
  }

  override def receive: Receive = {
    case PollByID(id) =>
      Logger.info(s"Polling energy sensor with id $id")
      val tags = Seq("device" -> "domoticz", "sensorId" -> id.toString)
      domoticzService.energy(id).map {
        case Left(message) => Error(message)
        case Right(measurement) => StoreWithTags(measurement, tags)
      } pipeTo storageActor
    case ReadingByID(id) =>
      Logger.info(s"Reading energy sensor with id $id")
      domoticzService.energy(id).map {
        case Left(message) => Error(message)
        case Right(measurement) => measurement
      } pipeTo sender
  }
}
