package models

case class GameResult (
    val id: Long, 
    val game: Game,
    val player: Player,
    val position: Int, 
    val stake: Option[Double],
    val winnings: Option[Double],
    val currency: Option[String]
) extends Model[Long]