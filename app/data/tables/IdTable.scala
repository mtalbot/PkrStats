package data.tables

import scala.slick.lifted.Column

trait IdTable[idType] {
	def id: Column[idType]
}