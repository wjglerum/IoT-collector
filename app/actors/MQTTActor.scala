package actors

import java.text.SimpleDateFormat

import akka.actor.{Actor, ActorRef, Props}
import akka.stream.ActorMaterializer
import akka.stream.alpakka.mqtt.scaladsl.MqttSource
import akka.stream.alpakka.mqtt.{MqttConnectionSettings, MqttQoS, MqttSourceSettings}
import akka.stream.scaladsl.{Keep, Sink}
import com.google.inject.Inject
import config.MQTTConfig
import models.{Message, RPIReading}
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import play.api.{Configuration, Logger}
import play.api.libs.json.{Format, Json}

class MQTTActor @Inject()(configuration: Configuration,
                          out: ActorRef) extends Actor {
  private final val dateFormatter: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private val MQTTConfig = configuration.get[MQTTConfig]("mqtt")

  override def preStart(): Unit = {
    super.preStart()
    self ! MqttSourceSettings(
      MqttConnectionSettings(
        s"tcp://${MQTTConfig.host}:${MQTTConfig.port}",
        MQTTConfig.topic,
        new MemoryPersistence
      ),
      Map(MQTTConfig.topic -> MqttQoS.atLeastOnce)
    )
  }

  override def receive: Receive = {
    case settings: MqttSourceSettings =>
      val mqttSource = MqttSource.atLeastOnce(settings, bufferSize = 8)
      mqttSource
        .map { message =>
          val payload = message.message.payload.utf8String
          Logger.info(s"RAW MQTT MESSAGE: $payload")

          val reading = Json.parse(payload).as[SensorReading]
          val measurement = RPIReading(
            timestamp = dateFormatter.parse(reading.timestamp).toInstant,
            temperature = reading.temperature,
            humidity = reading.humidity
          )
          Logger.info(measurement.toString)

          out ! measurement
        }
        .toMat(Sink.ignore)(Keep.none)
        .run()
    case message: Message => Logger.info(s"RAW WS MESSAGE: ${message.message}")
  }

  case class SensorReading(timestamp: String, sensor: String, temperature: Double, humidity: Double)

  object SensorReading {
    implicit val sensorFormat: Format[SensorReading] = Json.format[SensorReading]
  }

}

object MQTTActor {
  def props(out: ActorRef, configuration: Configuration) = Props(new MQTTActor(configuration, out))
}
