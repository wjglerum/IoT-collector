package controllers

import actors.MQTTActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.Inject
import models.{Message, RPIReading}
import play.api.Configuration
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc._

import scala.language.postfixOps

class HomeController @Inject()(cc: ControllerComponents, configuration: Configuration)
                              (implicit system: ActorSystem, mat: Materializer)
  extends AbstractController(cc) {

  implicit val messageFlowTransformer: MessageFlowTransformer[Message, RPIReading] = MessageFlowTransformer.jsonMessageFlowTransformer[Message, RPIReading]

  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def socket: WebSocket = WebSocket.accept[Message, RPIReading] { implicit request =>
    ActorFlow.actorRef { out =>
      MQTTActor.props(out, configuration)
    }
  }
}
