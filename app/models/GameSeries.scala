package models

import models.baseModels.Model
import models.baseModels.ChangeLoggedModel
import org.joda.time.DateTime
import models.gameTypes.GameType

case class GameSeries(
  val id: Long,
  val name: String,
  val defaultGameType: Option[GameType],
  val createdBy: Player,
  val createdOn: DateTime,
  val changedBy: Option[Player],
  val changedOn: Option[DateTime]) extends Model[Long] with ChangeLoggedModel