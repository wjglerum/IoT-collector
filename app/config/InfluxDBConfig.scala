package config

import com.typesafe.config.Config
import play.api.ConfigLoader

case class InfluxDBConfig(host: String,
                          port: Int,
                          username: String,
                          password: String,
                          schema: String,
                          defaultRetention: String,
                          database: String)

object InfluxDBConfig {
  implicit val configLoader: ConfigLoader[InfluxDBConfig] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)

    InfluxDBConfig(
      host = config.getString("host"),
      port = config.getInt("port"),
      username = config.getString("username"),
      password = config.getString("password"),
      schema = config.getString("schema"),
      defaultRetention = config.getString("defaultRetention"),
      database = config.getString("database")
    )
  }
}
