package actors

import java.time.Instant

import akka.actor.{Actor, ActorRef, ActorSystem}
import com.google.inject.Inject
import com.google.inject.name.Named
import io.waylay.influxdb.Influx.{IFloat, IPoint}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import services.{InfluxDBAPI, TadoAPI}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

case object PollTado
case object PollWeather

class TadoScheduler @Inject()(val actorSystem: ActorSystem,
                               @Named("tadoActor") val tadoActor: ActorRef)(implicit ec: ExecutionContext) {
  actorSystem.scheduler.schedule(0 minutes, 1 minute, tadoActor, PollTado)
  actorSystem.scheduler.schedule(0 minutes, 5 minute, tadoActor, PollWeather)
}

class TadoActor @Inject()(tadoAPI: TadoAPI,
                          configuration: Configuration,
                          ws: WSClient,
                          influxDBAPI: InfluxDBAPI)(implicit ec: ExecutionContext) extends Actor {

  def receive = {
    case PollTado => state
    case PollWeather => weather
  }

  private def weather = tadoAPI.weather.flatMap {
    case Left(error) => Future.successful(Logger.error(error))
    case Right(weather) =>
      Logger.info("--- Polling weather ---")
      val timestamp = Instant.parse(weather.outsideTemperature.timestamp)
      val temperature = IPoint(measurementName = "temperature", tags = Seq("device" -> "tado"), fields = Seq("celsius" -> IFloat(weather.outsideTemperature.celsius.toFloat)), timestamp = timestamp)
      val solarIntensity = IPoint(measurementName = "solarIntensity", tags = Seq("device" -> "tado"), fields = Seq("percentage" -> IFloat(weather.solarIntensity.percentage)), timestamp = timestamp)
      Logger.info(s"Temperature outside: ${weather.outsideTemperature.celsius}°C, solar intensity: ${weather.solarIntensity.percentage}%")
      influxDBAPI.store("weatherOutside", Seq(temperature, solarIntensity))
  }


  private def state = tadoAPI.state(1).flatMap {
    case Left(error) => Future.successful(Logger.error(error))
    case Right(state) =>
      Logger.info("--- Polling tado ---")
      val timestamp = Instant.parse(state.sensorDataPoints.insideTemperature.timestamp)
      val temperature = IPoint(measurementName = "temperature", tags = Seq("device" -> "tado"), fields = Seq("celsius" -> IFloat(state.sensorDataPoints.insideTemperature.celsius.toFloat)), timestamp = timestamp)
      val humidity = IPoint(measurementName = "humidity", tags = Seq("device" -> "tado"), fields = Seq("humidity" -> IFloat(state.sensorDataPoints.humidity.percentage.toFloat)), timestamp = timestamp)
      val heatingPower = IPoint(measurementName = "heatingPower", tags = Seq("device" -> "tado"), fields = Seq("heatingPower" -> IFloat(state.activityDataPoints.heatingPower.percentage)), timestamp = timestamp)
      Logger.info(s"Temperature: ${state.sensorDataPoints.insideTemperature.celsius}°C, humidity: ${state.sensorDataPoints.humidity.percentage}%, heatingPower: ${state.activityDataPoints.heatingPower.percentage}")
      influxDBAPI.store("tado", Seq(temperature, humidity, heatingPower))
  }
}
