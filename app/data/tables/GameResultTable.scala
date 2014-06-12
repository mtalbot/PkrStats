package data.tables

import data.helpers.DatabaseDriver.slickDriver._
import models.GameResult
import data.tables.GameTable.{ mapper => gameMapper }
import data.tables.PlayerTable.{ mapper => playerMapper }
import models.Game
import models.Player
import data.RequiresDatabaseConnection
import data.helpers.MappedColumnModelID

object GameResultTable extends SuppliesTableQuery[GameResultTable, GameResult, Long] with MappedColumnModelID[GameResult] {
  val tableQuery = TableQuery[GameResultTable]

  implicit val mapper = MappedColumnModelID[GameResultTable, GameResult, Long](this.tableQuery)
}

class GameResultTable(tag: Tag) extends Table[GameResult](tag, "GAME_RESULTS") with IdTable[Long] {
  val id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  val game = column[Game]("GAME_ID", O.NotNull)
  val player = column[Player]("PLAYER_ID", O.NotNull)
  val position = column[Int]("POSITION", O.NotNull)
  val stake = column[Option[Double]]("STAKE", O.Nullable)
  val winnings = column[Option[Double]]("WINNINGS", O.Nullable)
  val currency = column[Option[String]]("CURRENCY", O.Nullable)

  //val fkPlayer = foreignKey("FK_GAME_RESULTS_PLAYER", player.asColumnOf[Long], PlayerTable.tableQuery)(_.id)
  //val fkGame = foreignKey("FK_GAME_RESULTS_GAMES", game.asColumnOf[Long], GameTable.tableQuery)(_.id)

  def * = (id, game, player, position, stake, winnings, currency) <> (GameResult.tupled, GameResult.unapply)
}