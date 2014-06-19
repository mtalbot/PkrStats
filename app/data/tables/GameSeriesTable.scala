package data.tables

import data.helpers.DatabaseDriver.slickProfile._
import data.helpers.DatabaseDriver.jodaDriver._
import models.GameSeries
import data.helpers.MappedColumnModelID
import data.baseTables.IdTable
import data.baseTables.ChangeLoggedTable
import data.tables.PlayerTable.{ mapper => playerMapper }
import org.joda.time.DateTime
import models.Player
import models.GameType

object GameSeriesTable extends SuppliesTableQuery[GameSeriesTable, GameSeries, Long] with MappedColumnModelID[GameSeries] {
  val tableQuery = TableQuery[GameSeriesTable]
  
  implicit val mapper = MappedColumnModelID[GameSeriesTable, GameSeries, Long](this.tableQuery)
}

class GameSeriesTable(tag: Tag) extends Table[GameSeries](tag, "GAME_SERIES") with IdTable[Long] with ChangeLoggedTable {

  val id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  val name = column[String]("NAME", O.NotNull)
  val defaultGameType = column[Option[GameType.GameType]]("DEFAULT_GAME_TYPE")
  val createdBy = column[Player]("CREATED_BY", O.NotNull)
  val createdOn = column[DateTime]("CREATED", O.NotNull)
  val changedBy = column[Option[Player]]("CHANGED_BY")
  val changedOn = column[Option[DateTime]]("CHANGED")  
  
  def * = (id, name, defaultGameType, createdBy, createdOn, changedBy, changedOn) <> (GameSeries.tupled, GameSeries.unapply)
}