package models

import org.joda.time.DateTime
import models.gameTypes.GameType

case class Game(
  val id: Long,
  val series: GameSeries,
  val hosted: String,
  val date: DateTime,
  val gametype: GameType) extends baseModels.Model[Long] {
  override def toString = date.toString("y/M/d") + " @ " + hosted
}