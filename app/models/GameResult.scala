package models

case class GameResult (val id: Long, val game: Game, val position: Int, val player: Player) extends Model[Long]