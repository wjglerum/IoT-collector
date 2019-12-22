package config

import com.typesafe.config.Config
import play.api.ConfigLoader

import scala.jdk.CollectionConverters._

case class TadoConfig(clientId: String,
                      clientSecret: String,
                      password: String,
                      username: String,
                      homeId: Int,
                      zones: Seq[Int])

object TadoConfig {

  implicit val configLoader: ConfigLoader[TadoConfig] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)

    TadoConfig(
      clientId = config.getString("clientId"),
      clientSecret = config.getString("clientSecret"),
      password = config.getString("password"),
      username = config.getString("username"),
      homeId = config.getInt("homeId"),
      zones = config.getIntList("zones").asScala.map(_.toInt).toSeq
    )
  }
}
