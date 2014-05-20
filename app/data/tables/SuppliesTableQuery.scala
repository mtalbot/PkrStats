package data.tables

import scala.slick.driver.JdbcDriver.simple._
import models.Model

trait SuppliesTableQuery[tableType <: Table[modelType], modelType <: Model[idType], idType] {
	def tableQuery: TableQuery[tableType]
}