package data.helpers

import data.helpers.DatabaseDriver.slickDriver._

object MappedColumnStringList {
  implicit val stringMapper = MappedColumnType.base[List[String], String](
    { list =>
      list.map(str => str.replace(":", "::")).fold("")((left, right) => (left, right) match {
        case (left, right) if left == null || left.isEmpty() => right
        case (left, right) if right == null || right.isEmpty() => left
        case (left, right) if (right == null || right.isEmpty()) && (left == null || left.isEmpty()) => ""
        case _ => left + ":" + right
      })
    },
    { str => (str.split("(?<!:):(?!:)")).map(str => str.replace("::", ":")).toList })
}