package controllers

import javax.inject._

import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.mvc._
import services.{DomoticzService, InfluxDBService, TadoService}

import scala.concurrent.ExecutionContext

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               configuration: Configuration,
                               ws: WSClient,
                               tadoAPI: TadoService,
                               domoticzService: DomoticzService,
                               influxDBService: InfluxDBService)(implicit ec: ExecutionContext) extends AbstractController(cc) {

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

  def weather: Action[AnyContent] = Action.async {
    tadoAPI.weather.map {
      case Left(error) => BadRequest(error)
      case Right(weather) => Ok(weather.toString)
    }
  }

  def state(id: Int): Action[AnyContent] = Action.async {
    tadoAPI.state(id).map {
      case Left(error) => BadRequest(error)
      case Right(state) => Ok(state.toString)
    }
  }

  def energy(id: Int): Action[AnyContent] = Action.async {
    domoticzService.utility(id).map {
      case Left(error) => BadRequest(error)
      case Right(state) => Ok(state.toString)
    }
  }
}
