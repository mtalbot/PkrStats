package models.baseModels

import models.Player
import org.joda.time.DateTime

trait ChangeLoggedModel {
	val createdBy: Player
	val createdOn: DateTime
	val changedBy: Option[Player]
	val changedOn: Option[DateTime]
}