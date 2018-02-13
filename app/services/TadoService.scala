package services

import com.google.inject.Inject
import config.TadoConfig
import models.{AuthResponse, State, Weather}
import play.api.Configuration
import play.api.http.Status.OK
import play.api.libs.json.Reads
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class TadoService @Inject()(configuration: Configuration,
                            ws: WSClient)(implicit ec: ExecutionContext) {

  type Response[T] = Either[String, T]
  type ResponseF[T] = Future[Response[T]]

  private final val tadoConfig: TadoConfig = configuration.get[TadoConfig]("tado")

  def state(zoneId: Int): ResponseF[State] = get[State](s"homes/${tadoConfig.homeId}/zones/$zoneId/state")

  def weather: ResponseF[Weather] = get[Weather](s"homes/${tadoConfig.homeId}/weather")

  private def get[T](path: String)(implicit tjs: Reads[T]): ResponseF[T] = {
    getToken.flatMap {
      case Left(error) => Future.successful(Left(error))
      case Right(authResponse) => withAuth[T](path, authResponse.accessToken)
    }
  }

  private def withAuth[T](path: String, token: String)(implicit tjs: Reads[T]): ResponseF[T] = {
    val headers = Map(
      "Authorization" -> s"Bearer $token"
    )

    ws.url(s"https://my.tado.com/api/v2/$path").addHttpHeaders(headers.toSeq: _*).get.map(processResponse[T])
  }

  private def getToken: ResponseF[AuthResponse] = {
    val parameters = Map(
      "client_id" -> tadoConfig.clientId,
      "client_secret" -> tadoConfig.clientSecret,
      "grant_type" -> "password",
      "password" -> tadoConfig.password,
      "scope" -> "home.user",
      "username" -> tadoConfig.username
    )

    ws.url("https://auth.tado.com/oauth/token").post(parameters).map(processResponse[AuthResponse])
  }

  private def processResponse[T](response: WSResponse)(implicit tjs: Reads[T]): Response[T] = {
    response.status match {
      case OK => Right(response.json.as[T])
      case _ => Left(response.body)
    }
  }
}
