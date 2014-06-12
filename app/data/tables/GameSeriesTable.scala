package data.tables

import data.helpers.DatabaseDriver.slickDriver._
import models.GameSeries
import data.helpers.MappedColumnModelID

object GameSeriesTable extends SuppliesTableQuery[GameSeriesTable, GameSeries, Long] with MappedColumnModelID[GameSeries] {
  val tableQuery = TableQuery[GameSeriesTable]
  
  implicit val mapper = MappedColumnModelID[GameSeriesTable, GameSeries, Long](this.tableQuery)
}

class GameSeriesTable(tag: Tag) extends Table[GameSeries](tag, "GAME_SERIES") with IdTable[Long] {

  val id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  val name = column[String]("NAME", O.NotNull)
  
  def * = (id, name) <> (GameSeries.tupled, GameSeries.unapply)
}