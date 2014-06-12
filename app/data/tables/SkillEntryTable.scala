package data.tables

import data.helpers.DatabaseDriver.slickDriver._
import models._
import data.tables.GameResultTable.mapper
import data.helpers.MappedColumnModelID

object SkillEntryTable extends SuppliesTableQuery[SkillEntryTable, SkillEntry, Long] with MappedColumnModelID[SkillEntry] {
  override val tableQuery = TableQuery[SkillEntryTable]

  implicit override val mapper = MappedColumnModelID[SkillEntryTable, SkillEntry, Long](this.tableQuery)
}

class SkillEntryTable(tag: Tag) extends Table[SkillEntry](tag, "SKILL_ENTRIES") with IdTable[Long] { 
  val id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  val game = column[GameResult]("GAME_RESULT_ID", O.NotNull)
  val mean = column[Double]("MEAN", O.NotNull)
  val stddev = column[Double]("STDDEV", O.NotNull)

  //val fkGameResult = foreignKey("FK_SKILL_ENTRY_GAME_RESULT", game.asColumnOf[Long], GameResultTable.tableQuery)(_.id)

  def * = (id, game, mean, stddev) <> (SkillEntry.tupled, SkillEntry.unapply)
}