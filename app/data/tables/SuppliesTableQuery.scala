package data.tables

import data.helpers.DatabaseDriver.slickProfile._
import models.baseModels.Model

trait SuppliesTableQuery[tableType <: Table[modelType], modelType <: Model[idType], idType] {
	def tableQuery: TableQuery[tableType]
}