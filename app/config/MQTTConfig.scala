package config

import com.typesafe.config.Config
import play.api.ConfigLoader

case class MQTTConfig(host: String, port: Int, topic: String)

object MQTTConfig {

  implicit val configLoader: ConfigLoader[MQTTConfig] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)
    MQTTConfig(
      host = config.getString("host"),
      port = config.getInt("port"),
      topic = config.getString("topic")
    )
  }
}
