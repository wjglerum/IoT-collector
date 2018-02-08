package models

import com.typesafe.config.Config
import play.api.ConfigLoader

case class TadoConfig(url: String,
                      authUrl: String,
                      clientId: String,
                      clientSecret: String,
                      password: String,
                      username: String,
                      homeId: Int)

object TadoConfig {

  implicit val configLoader: ConfigLoader[TadoConfig] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)

    TadoConfig(
      url = config.getString("url"),
      authUrl = config.getString("authUrl"),
      clientId = config.getString("clientId"),
      clientSecret = config.getString("clientSecret"),
      password = config.getString("password"),
      username = config.getString("username"),
      homeId = config.getInt("homeId")
    )
  }
}
