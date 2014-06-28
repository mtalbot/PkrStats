package data.helpers

import data.helpers.DatabaseDriver.slickProfile._
import components.JsonSupport
import models.gameTypes.GameTypes
import models.gameTypes.GameType

object MappedColumnGameType {
  implicit val gameTypeMapper = MappedColumnType.base[GameType, String](
    { game => GameTypes.toString(game) },
    { str => GameTypes.fromString(str) })
}