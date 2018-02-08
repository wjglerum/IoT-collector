package controllers

import java.time.Instant
import javax.inject._

import io.waylay.influxdb.Influx.{IFloat, IPoint}
import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.mvc._
import services.{InfluxDBAPI, TadoAPI}

import scala.concurrent.{ExecutionContext, Future}

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               tadoAPI: TadoAPI,
                               configuration: Configuration,
                               ws: WSClient,
                               influxDBAPI: InfluxDBAPI)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def weather = Action.async {
    tadoAPI.weather.flatMap {
      case Left(error) => Future.successful(BadRequest(error))
      case Right(weather) =>
        val timestamp = Instant.parse(weather.outsideTemperature.timestamp)
        val temperature = IPoint(measurementName = "temperature", tags = Seq("device" -> "tado"), fields = Seq("celsius" -> IFloat(weather.outsideTemperature.celsius.toFloat)), timestamp = timestamp)
        val solarIntensity = IPoint(measurementName = "solarIntensity", tags = Seq("device" -> "tado"), fields = Seq("percentage" -> IFloat(weather.solarIntensity.percentage)), timestamp = timestamp)
        influxDBAPI.store("weatherOutside", Seq(temperature, solarIntensity)).map(_ => Ok(weather.toString))
    }
  }

  def state = Action.async {
    tadoAPI.state(1).flatMap {
      case Left(error) => Future.successful(BadRequest(error))
      case Right(state) =>
        val timestamp = Instant.parse(state.sensorDataPoints.insideTemperature.timestamp)
        val temperature = IPoint(measurementName = "temperature", tags = Seq("device" -> "tado"), fields = Seq("celsius" -> IFloat(state.sensorDataPoints.insideTemperature.celsius.toFloat)), timestamp = timestamp)
        val humidity = IPoint(measurementName = "humidity", tags = Seq("device" -> "tado"), fields = Seq("humidity" -> IFloat(state.sensorDataPoints.humidity.percentage.toFloat)), timestamp = timestamp)
        val heatingPower = IPoint(measurementName = "heatingPower", tags = Seq("device" -> "tado"), fields = Seq("heatingPower" -> IFloat(state.activityDataPoints.heatingPower.percentage)), timestamp = timestamp)
        influxDBAPI.store("tado", Seq(temperature, humidity, heatingPower)).map(_ => Ok(state.toString))
    }
  }
}
