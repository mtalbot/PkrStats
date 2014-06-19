package data.baseTables

import scala.slick.lifted.Column

trait IdTable[idType] {
  def id: Column[idType]
}