package modules

import actors.{DomoticzActor, SchedulerActor, TadoActor, WeatherActor}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class SchedulerModule extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bindActor[DomoticzActor]("domoticzActor")
    bindActor[TadoActor]("tadoActor")
    bindActor[WeatherActor]("weatherActor")
    bind(classOf[SchedulerActor]).asEagerSingleton()
  }
}