package data.tables

import scala.slick.driver.JdbcDriver.simple._
import models.GameResult
import data.tables.GameTable.{mapper => gameMapper}
import data.tables.PlayerTable.{mapper => playerMapper}
import models.Game
import models.Player
import data.RequiresDatabaseConnection

object GameResultTable extends SuppliesTableQuery[GameResultTable, GameResult, Long] with MappedColumnModelID[GameResult] {
  val tableQuery = TableQuery[GameResultTable]
  
  implicit val mapper = MappedColumnModelID[GameResult, Long](this)
  
  override def getById(id: Long)(implicit session: Session): Option[GameResult] = tableQuery.findBy(_.id).apply(id).firstOption
}

class GameResultTable(tag: Tag) extends Table[GameResult](tag, "GAME_RESULTS") { 
	val id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
	val game = column[Game]("GAME_ID", O.NotNull)
	val position = column[Int]("POSITION", O.NotNull)
	val player = column[Player]("PLAYER_ID", O.NotNull)
	
	val idx = index("IDX_GAME_RESULT", (game, position), false)
	val fkPlayer = foreignKey("FK_GAME_RESULTS_PLAYER", player.asColumnOf[Long], PlayerTable.tableQuery)(_.id)
	val fkGame = foreignKey("FK_GAME_RESULTS_GAMES", game.asColumnOf[Long], GameTable.tableQuery)(_.id)
	
	def * = (id, game, position, player) <> (GameResult.tupled, GameResult.unapply)
}