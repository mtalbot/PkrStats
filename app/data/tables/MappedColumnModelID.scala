package data.tables

import scala.slick.driver.JdbcDriver.simple._
import models.Model
import scala.reflect.ClassTag
import data.RequiresDatabaseConnection

trait MappedColumnModelID[modelType <: Model[_]] {
  def mapper: BaseColumnType[modelType]
}

object MappedColumnModelID extends RequiresDatabaseConnection {
  def apply[modelType <: Model[idType], idType](tableObject: SuppliesTableQuery[Table[modelType], modelType, idType])(implicit idClass: ClassTag[idType], modelClass: ClassTag[modelType], idcol: BaseColumnType[idType]): BaseColumnType[modelType] = {
    MappedColumnType.base[modelType, idType](
      { model => model.id },
      { id => this.db.withSession { implicit session => tableObject.getById(id).get } })
  }
}