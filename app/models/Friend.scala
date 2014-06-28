package models

import models.baseModels.Model

case class Friend(val id: Long, val player: Player, val friend: Player) extends Model[Long]