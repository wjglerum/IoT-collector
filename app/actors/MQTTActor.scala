package actors

import java.text.SimpleDateFormat

import actors.StorageActor.StoreWithTags
import akka.actor.{Actor, ActorRef}
import akka.stream.ActorMaterializer
import akka.stream.alpakka.mqtt.scaladsl.MqttSource
import akka.stream.alpakka.mqtt.{MqttConnectionSettings, MqttQoS, MqttSourceSettings}
import akka.stream.scaladsl.{Keep, Sink}
import com.google.inject.Inject
import com.google.inject.name.Named
import config.MQTTConfig
import models.RPI
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import play.api.libs.json.{Format, Json}
import play.api.{Configuration, Logger}

class MQTTActor @Inject()(configuration: Configuration,
                          @Named("storageActor") storageActor: ActorRef) extends Actor {

  private val MQTTConfig = configuration.get[MQTTConfig]("mqtt")
  private implicit val materializer = ActorMaterializer()
  private final val dateFormatter: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

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
          val reading = Json.parse(message.message.payload.toArray).as[SensorReading]
          val measurement = RPI(
            timestamp = dateFormatter.parse(reading.timestamp).toInstant,
            temperature = reading.temperature,
            humidity = reading.humidity
          )
          Logger.info(measurement.toString)
          val tags = Seq(
            "sensor" -> reading.sensor
          )
          storageActor ! StoreWithTags(measurement, tags)
        }
        .toMat(Sink.ignore)(Keep.none)
        .run()
  }

  case class SensorReading(timestamp: String, sensor: String, temperature: Double, humidity: Double)

  object SensorReading {
    implicit val sensorFormat: Format[SensorReading] = Json.format[SensorReading]
  }

}

