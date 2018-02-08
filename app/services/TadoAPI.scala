package services

import com.google.inject.Inject
import models.{AuthResponse, State, TadoConfig, Weather}
import play.api.http.Status
import play.api.libs.json.Reads
import play.api.libs.ws.ahc.AhcCurlRequestLogger
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

class TadoAPI @Inject()(configuration: Configuration,
                        ws: WSClient)(implicit ec: ExecutionContext) {

  type Response[T] = Either[String, T]
  type ResponseF[T] = Future[Response[T]]

  private final val tadoConfig: TadoConfig = configuration.get[TadoConfig]("tado")

  def state(zoneId: Int): ResponseF[State] = withAuth[State](s"/homes/${tadoConfig.homeId}/zones/$zoneId/state")

  def weather: ResponseF[Weather] = withAuth[Weather](s"/homes/${tadoConfig.homeId}/weather")

  private def withAuth[T](path: String)(implicit tjs: Reads[T]): ResponseF[T] = {
    getToken.flatMap {
      case Left(error) => Future.successful(Left(error))
      case Right(authResponse) => withAuth[T](path, authResponse.accessToken)
    }
  }

  private def withAuth[T](path: String, token: String)(implicit tjs: Reads[T]): ResponseF[T] = {
    val headers = Map(
      "Authorization" -> s"Bearer $token"
    )

    ws.url(tadoConfig.url + path).withRequestFilter(AhcCurlRequestLogger())
      .addHttpHeaders(headers.toSeq: _*).get.map(processResponse[T])
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

    ws.url(tadoConfig.authUrl).post(parameters).map(processResponse[AuthResponse])
  }

  private def refreshToken(refreshToken: String): ResponseF[AuthResponse] = {
    val parameters = Map(
      "refresh_token" -> refreshToken,
      "grant_type" -> "refresh_token"
    )

    ws.url(tadoConfig.authUrl).post(parameters).map(processResponse[AuthResponse])
  }

  private def processResponse[T](response: WSResponse)(implicit tjs: Reads[T]): Response[T] = {
    response.status match {
      case Status.OK =>
        Logger.info(response.body)
        Right(response.json.as[T])
      case _ =>
        Logger.error(response.body)
        Left(response.body)
    }
  }
}
