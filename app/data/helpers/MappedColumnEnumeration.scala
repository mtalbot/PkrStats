package data.helpers

import data.helpers.DatabaseDriver.slickDriver._

trait MappedColumnEnumeration extends Enumeration {
  implicit def enumMapper = MappedColumnType.base[Value, Int](
    { enum => enum.id },
    { id => this(id) })
}