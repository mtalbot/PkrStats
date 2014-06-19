package data.tables

import data.helpers.DatabaseDriver.slickProfile._
import models.GameResult
import data.tables.GameTable.{ mapper => gameMapper }
import data.tables.PlayerTable.{ mapper => playerMapper }
import models.Game
import models.Player
import data.RequiresDatabaseConnection
import data.helpers.MappedColumnModelID
import data.baseTables.IdTable

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
  val score = column[Option[Double]]("SCORE", O.Nullable)

  def * = (id, game, player, position, stake, score) <> (GameResult.tupled, GameResult.unapply)
}