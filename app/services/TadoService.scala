package services

import java.time.Instant
import java.util.Date

import com.google.inject.Inject
import config.TadoConfig
import models.{OutsideWeather, Thermostat}
import play.api.Configuration
import play.api.http.Status.OK
import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json.{Format, Json, JsonConfiguration, Reads}
import play.api.libs.ws.{WSClient, WSResponse}
import services.TadoService.{AuthResponse, State, Weather}

import scala.concurrent.{ExecutionContext, Future}

class TadoService @Inject()(configuration: Configuration,
                            ws: WSClient)(implicit ec: ExecutionContext) {

  type Response[T] = Either[String, T]
  type ResponseF[T] = Future[Response[T]]

  private final val tadoConfig: TadoConfig = configuration.get[TadoConfig]("tado")

  def thermostat(id: Int): ResponseF[Thermostat] = get[State](s"homes/${tadoConfig.homeId}/zones/$id/state").map {
    case Left(error) => Left(error)
    case Right(state) => Right(stateToThermostat(state))
  }

  private def stateToThermostat(state: State) = Thermostat(
    timestamp = Instant.parse(state.sensorDataPoints.insideTemperature.timestamp),
    temperature = state.sensorDataPoints.insideTemperature.celsius,
    humidity = state.sensorDataPoints.humidity.percentage,
    heatingPower = state.activityDataPoints.heatingPower.percentage
  )

  def weather: ResponseF[OutsideWeather] = get[Weather](s"homes/${tadoConfig.homeId}/weather").map {
    case Left(error) => Left(error)
    case Right(weather) => Right(weatherToOutsideWeather(weather))
  }

  private def weatherToOutsideWeather(weather: Weather) = OutsideWeather(
    timestamp = Instant.parse(weather.outsideTemperature.timestamp),
    temperature = weather.outsideTemperature.celsius,
    solarIntensity = weather.solarIntensity.percentage
  )

  private def get[T](path: String)(implicit tjs: Reads[T]): ResponseF[T] = {
    getToken.flatMap {
      case Left(error) => Future.successful(Left(error))
      case Right(authResponse) => withAuth[T](path, authResponse.accessToken)
    }.recover {
      case t => Left(t.getMessage)
    }
  }

  private def withAuth[T](path: String, token: String)(implicit tjs: Reads[T]): ResponseF[T] = {
    val headers = Map(
      "Authorization" -> s"Bearer $token"
    )

    ws.url(s"https://my.tado.com/api/v2/$path").addHttpHeaders(headers.toSeq: _*).get.map(processResponse[T])
  }

  private def processResponse[T](response: WSResponse)(implicit tjs: Reads[T]): Response[T] = {
    response.status match {
      case OK => Right(response.json.as[T])
      case _ => Left(response.body)
    }
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
}

object TadoService {

  sealed trait DataPoint

  case class AuthResponse(accessToken: String,
                          expiresIn: Date,
                          jti: String,
                          refreshToken: String,
                          scope: String,
                          tokenType: String)

  case class State(activityDataPoints: ActivityDataPoints,
                   sensorDataPoints: SensorDataPoints,
                   setting: Setting)

  case class Setting(temperature: Option[Temperature])

  case class Temperature(celsius: Double)

  case class ActivityDataPoints(heatingPower: PercentageDataPoint)

  case class SensorDataPoints(humidity: PercentageDataPoint, insideTemperature: TemperatureDataPoint)

  case class PercentageDataPoint(percentage: Double, timestamp: String) extends DataPoint

  case class TemperatureDataPoint(celsius: Double, timestamp: String) extends DataPoint

  case class Weather(outsideTemperature: TemperatureDataPoint, solarIntensity: PercentageDataPoint)

  object AuthResponse {
    implicit val config: JsonConfiguration.Aux[Json.MacroOptions] = JsonConfiguration(SnakeCase)
    implicit val authResponseFormat: Format[AuthResponse] = Json.format[AuthResponse]
  }

  implicit val temperatureFormat: Format[Temperature] = Json.format[Temperature]
  implicit val settingFormat: Format[Setting] = Json.format[Setting]
  implicit val percentageDataPointFormat: Format[PercentageDataPoint] = Json.format[PercentageDataPoint]
  implicit val temperatureDataPointFormat: Format[TemperatureDataPoint] = Json.format[TemperatureDataPoint]
  implicit val activityDataPointsFormat: Format[ActivityDataPoints] = Json.format[ActivityDataPoints]
  implicit val sensorDataPointsFormat: Format[SensorDataPoints] = Json.format[SensorDataPoints]
  implicit val weatherFormat: Format[Weather] = Json.format[Weather]
  implicit val stateFormat: Format[State] = Json.format[State]
}
