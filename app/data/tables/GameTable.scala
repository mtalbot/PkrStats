package data.tables

import data.helpers.DatabaseDriver.slickProfile._
import models.Game
import org.joda.time.DateTime
import data.tables.GameSeriesTable._
import models.GameSeries
import data.helpers.MappedColumnModelID
import data.helpers.DatabaseDriver.jodaDriver._
import data.helpers.MappedColumnGameType.gameTypeMapper
import models.gameTypes.GameType

object GameTable extends SuppliesTableQuery[GameTable, Game, Long] with MappedColumnModelID[Game] {
  val tableQuery = TableQuery[GameTable]

  implicit val mapper = MappedColumnModelID[GameTable, Game, Long](this.tableQuery)
}

class GameTable(tag: Tag) extends Table[Game](tag, "GAMES") with IdTable[Long] {
  val id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  val series = column[GameSeries]("GAME_SERIES_ID", O.NotNull)
  val hosted = column[String]("HOSTED", O.NotNull)
  val date = column[DateTime]("DATE", O.NotNull)
  val gameType = column[GameType]("TYPE", O.NotNull)

  //val fk_series = foreignKey("FK_GAME_GAME_SERIES", series.asColumnOf[Long], GameSeriesTable.tableQuery)(_.id)

  def * = (id, series, hosted, date, gameType) <> (Game.tupled, Game.unapply)
}