package actors

import akka.actor.{Actor, Timers}

trait Sensor extends Actor with Timers

object Sensor {

  case class PollByID(id: Int)

  case class ReadingByID(id: Int)

  case class Error(message: String)

  case object Poll

  case object Reading

  case class PollKey(id: Int)

}
