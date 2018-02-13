package actors

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.Inject
import com.google.inject.name.Named

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

class SchedulerActor @Inject()(val actorSystem: ActorSystem,
                               @Named("tadoActor") val tadoActor: ActorRef,
                               @Named("weatherActor") val weatherActor: ActorRef,
                               @Named("domoticzActor") val domoticzActor: ActorRef)(implicit ec: ExecutionContext) {

  actorSystem.scheduler.schedule(0 minutes, 1 minute, tadoActor, Poll(1))
  actorSystem.scheduler.schedule(0 minutes, 5 minute, weatherActor, Poll)
  actorSystem.scheduler.schedule(0 minutes, 10 seconds, domoticzActor, Poll(1))
}
