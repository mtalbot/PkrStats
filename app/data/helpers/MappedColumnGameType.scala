package data.helpers

import data.helpers.DatabaseDriver.slickProfile._
import components.JsonSupport
import models.gameTypes.GameType

object MappedColumnGameType {
  implicit val gameTypeMapper = MappedColumnType.base[GameType, String](
    { list => JsonSupport.mapper.writeValueAsString(list) },
    { str => JsonSupport.mapper.readValue(str, classOf[GameType]) })
}