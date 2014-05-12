package data.tables

import scala.slick.driver.JdbcDriver.simple._
import models._
import data.tables.GameResultTable.mapper

object SkillEntryTable extends SuppliesTableQuery[SkillEntryTable, SkillEntry, Long] with MappedColumnModelID[SkillEntry] {
  val tableQuery = TableQuery[SkillEntryTable]

  implicit val mapper = MappedColumnModelID[SkillEntry, Long](this)
  
  override def getById(id: Long)(implicit session: Session): Option[SkillEntry] = tableQuery.findBy(_.id).apply(id).firstOption
}

class SkillEntryTable(tag: Tag) extends Table[SkillEntry](tag, "SKILL_ENTRIES") {
  val id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  val game = column[GameResult]("GAME_RESULT_ID", O.NotNull)
  val mean = column[Double]("MEAN", O.NotNull)
  val stddev = column[Double]("STDDEV", O.NotNull)

  val fkGameResult = foreignKey("FK_SKILL_ENTRY_GAME_RESULT", game.asColumnOf[Long], GameResultTable.tableQuery)(_.id)

  def * = (id, game, mean, stddev) <> (SkillEntry.tupled, SkillEntry.unapply)
}