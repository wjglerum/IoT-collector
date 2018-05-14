package modules

import actors._
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class IoTModule extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bindActor[StorageActor]("storageActor")
    //bindActor[EnergySensor]("energySensor")
    //bindActor[ThermostatSensor]("thermostatSensor")
    //bindActor[WeatherSensor]("weatherSensor")
    bindActor[MQTTActor]("mqttSensor")
  }
}
