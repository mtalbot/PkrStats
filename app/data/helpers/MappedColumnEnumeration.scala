package data.helpers

import scala.slick.driver.JdbcDriver.simple._

trait MappedColumnEnumeration extends Enumeration {
  implicit def enumMapper = MappedColumnType.base[Value, Int](
    { enum => enum.id },
    { id => this(id) })
}