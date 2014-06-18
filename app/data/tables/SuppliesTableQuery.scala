package data.tables

import data.helpers.DatabaseDriver.slickProfile._
import models.Model

trait SuppliesTableQuery[tableType <: Table[modelType], modelType <: Model[idType], idType] {
	def tableQuery: TableQuery[tableType]
}