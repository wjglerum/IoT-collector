package config

import com.typesafe.config.Config
import play.api.ConfigLoader

import scala.collection.JavaConverters._

case class DomoticzConfig(host: String, port: Int, sensors: Seq[Int])

object DomoticzConfig {

  implicit val configLoader: ConfigLoader[DomoticzConfig] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)
    DomoticzConfig(
      host = config.getString("host"),
      port = config.getInt("port"),
      sensors = config.getIntList("sensors").asScala.map(_.toInt)
    )
  }
}
