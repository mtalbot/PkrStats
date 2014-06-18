package data.helpers

import data.helpers.DatabaseDriver.slickProfile._
import components.JsonSupport

object MappedColumnStringList {
  implicit val stringMapper = MappedColumnType.base[List[String], String](
    { list => JsonSupport.mapper.writeValueAsString(list) },
    { str => JsonSupport.mapper.readValue(str, classOf[List[String]]) })
}