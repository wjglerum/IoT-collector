package actors

import actors.Sensor.Error
import actors.StorageActor.{Store, StoreWithTags}
import akka.actor.Actor
import com.google.inject.Inject
import config.InfluxDBConfig
import io.waylay.influxdb.InfluxDB
import models.Measurement
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class StorageActor @Inject()(configuration: Configuration,
                             ws: WSClient)(implicit ec: ExecutionContext) extends Actor {

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
        case Success(_) => Logger.info(s"Stored measurement $measurement with tags $tags")
        case Failure(t) => Logger.error(t.getMessage)
      }
    case Error(message) => Logger.error(message)
  }
}

object StorageActor {

  case class Store(measurement: Measurement)

  case class StoreWithTags(measurement: Measurement, tags: Seq[(String, String)])

}
