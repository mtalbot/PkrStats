package models

case class GameResult (
    val id: Long, 
    val game: Game,
    val player: Player,
    val position: Int, 
    val stake: Option[Double],
    val score: Option[Double]
) extends Model[Long]