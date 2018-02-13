package services

import com.google.inject.Inject
import config.DomoticzConfig
import models.{DomoticzResult, Energy}
import play.api.Configuration
import play.api.http.Status.OK
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class DomoticzService @Inject()(configuration: Configuration,
                                ws: WSClient)(implicit ec: ExecutionContext) {

  private final val domoticzConfig: DomoticzConfig = configuration.get[DomoticzConfig]("domoticz")
  private final val domiticzUrl: String = s"http://${domoticzConfig.host}:${domoticzConfig.port}"

  def utility(idx: Int): Future[Either[String, Energy]] = {
    ws.url(s"$domiticzUrl/json.htm?type=devices&rid=$idx").get().map { response =>
      response.status match {
        case OK => response.json.as[DomoticzResult[Energy]].result.headOption.toRight("No measurement found")
        case _ => Left(response.body)
      }
    }
  }
}
