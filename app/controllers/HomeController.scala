package controllers

import actors.Sensor.{Reading, ReadingByID}
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Inject
import com.google.inject.name.Named
import models.{Energy, OutsideWeather, Thermostat}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

class HomeController @Inject()(@Named("energySensor") energySensor: ActorRef,
                               @Named("thermostatSensor") thermostatSensor: ActorRef,
                               @Named("weatherSensor") weatherSensor: ActorRef,
                               cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  implicit private val timeout: Timeout = 5 seconds

  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def energy(id: Int) = Action.async { implicit request =>
    (energySensor ? ReadingByID(id)).mapTo[Energy].map { measurement =>
      Ok(Json.toJson(measurement))
    }.recover {
      case t => Ok(t.getMessage)
    }
  }

  def thermostat(id: Int) = Action.async { implicit request =>
    (thermostatSensor ? ReadingByID(id)).mapTo[Thermostat].map { measurement =>
      Ok(Json.toJson(measurement))
    }.recover {
      case t => Ok(t.getMessage)
    }
  }

  def weather = Action.async { implicit request =>
    (weatherSensor ? Reading).mapTo[OutsideWeather].map { weather =>
      Ok(Json.toJson(weather))
    }.recover {
      case t => Ok(t.getMessage)
    }
  }
}
