package data.tables

import scala.slick.driver.JdbcDriver.simple._
import models.Game
import org.joda.time.DateTime
import com.github.tototoshi.slick.JdbcJodaSupport._

object GameTable extends SuppliesTableQuery[GameTable, Game, Long] with MappedColumnModelID[Game] {
  val tableQuery = TableQuery[GameTable]
  
  implicit val mapper = MappedColumnModelID[Game, Long](this)
  
  override def getById(id: Long)(implicit session: Session): Option[Game] = tableQuery.findBy(_.id).apply(id).firstOption
}

class GameTable(tag: Tag) extends Table[Game](tag, "GAMES") {
	val id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
	val hosted = column[String]("HOSTED", O.NotNull)
	val date = column[DateTime]("DATE", O.NotNull)
	
	def * = (id, hosted, date) <> (Game.tupled, Game.unapply)
}