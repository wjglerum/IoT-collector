package config

import com.typesafe.config.Config
import play.api.ConfigLoader

case class DomoticzConfig(host: String, port: Int)

object DomoticzConfig {

  implicit val configLoader: ConfigLoader[DomoticzConfig] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)

    DomoticzConfig(
      host = config.getString("host"),
      port = config.getInt("port")
    )
  }
}
