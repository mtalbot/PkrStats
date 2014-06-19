package data.baseTables

import scala.slick.lifted.Column
import models.Player
import org.joda.time.DateTime

trait ChangeLoggedTable {
	val createdBy: Column[Player]
	val createdOn: Column[DateTime]
	val changedBy: Column[Option[Player]]
	val changedOn: Column[Option[DateTime]]
}