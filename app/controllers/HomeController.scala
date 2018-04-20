package controllers


import actors.Sensor.{Reading, ReadingByID, Error => SensorError}
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Inject
import com.google.inject.name.Named
import models.{Energy, OutsideWeather, Thermostat}
import play.api.libs.json.{Json, Writes}
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
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
    process[Energy](energySensor ? ReadingByID(id))
  }

  def thermostat(id: Int) = Action.async { implicit request =>
    process[Thermostat](thermostatSensor ? ReadingByID(id))
  }

  def weather = Action.async { implicit request =>
    process[OutsideWeather](weatherSensor ? Reading)
  }

  private def process[T](request: Future[Any])(implicit tjs: Writes[T]): Future[Result] = request.map {
    case measurement: Energy => Ok(Json.toJson(measurement))
    case measurement: Thermostat => Ok(Json.toJson(measurement))
    case measurement: OutsideWeather => Ok(Json.toJson(measurement))
    case error: SensorError => BadRequest(error.message)
    case _ => InternalServerError("Unknown message received")
  }
}
