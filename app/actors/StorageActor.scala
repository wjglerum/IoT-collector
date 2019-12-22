package actors

import actors.Sensor.Error
import actors.StorageActor.{Store, StoreWithTags}
import akka.actor.Actor
import com.google.inject.Inject
import config.InfluxDBConfig
import io.waylay.influxdb.InfluxDB
import models.Measurement
import play.api.libs.ws.StandaloneWSClient
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class StorageActor @Inject()(configuration: Configuration,
                             ws: StandaloneWSClient)(implicit ec: ExecutionContext) extends Actor {

  private val logger: Logger = Logger(this.getClass)
  private val influxDBConfig: InfluxDBConfig = configuration.get[InfluxDBConfig]("influxdb")
  private val db = new InfluxDB(
    ws = ws,
    host = influxDBConfig.host,
    port = influxDBConfig.port,
    username = influxDBConfig.username,
    password = influxDBConfig.password,
    schema = influxDBConfig.schema,
    defaultRetention = influxDBConfig.defaultRetention
  )

  override def receive: Receive = {
    case Store(measurement) => self ! StoreWithTags(measurement, Nil)
    case StoreWithTags(measurement, tags) =>
      db.storeAndMakeDbIfNeeded(influxDBConfig.database, Seq(measurement.toPointWithTags(tags))) onComplete {
        case Success(_) => logger.info(s"Stored measurement $measurement with tags $tags")
        case Failure(t) => logger.error(t.getMessage)
      }
    case Error(message) => logger.error(message)
  }
}

object StorageActor {

  case class Store(measurement: Measurement)

  case class StoreWithTags(measurement: Measurement, tags: Seq[(String, String)])

}
