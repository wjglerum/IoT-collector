package modules

import actors.{TadoActor, TadoScheduler}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class TadoModule extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bindActor[TadoActor]("tadoActor")
    bind(classOf[TadoScheduler]).asEagerSingleton()
  }
}