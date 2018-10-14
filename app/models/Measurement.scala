package models

import java.time.Instant

sealed trait Measurement

case class RPIReading(timestamp: Instant, temperature: Double, humidity: Double) extends Measurement

case class Message(message: String)
