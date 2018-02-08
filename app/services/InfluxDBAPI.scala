package services

import com.google.inject.Inject
import io.waylay.influxdb.Influx.IPoint
import io.waylay.influxdb.{Influx, InfluxDB}
import models.InfluxDBConfig
import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class InfluxDBAPI @Inject()(configuration: Configuration,
                            ws: WSClient)(implicit ec: ExecutionContext) {

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

  def query(databaseName: String, query: String): Future[Influx.Results] = db.query(databaseName, query)

  def store(databaseName: String, points: Seq[IPoint]): Future[Unit] = db.storeAndMakeDbIfNeeded(databaseName, points)

}
