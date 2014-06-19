package data.helpers

import data.helpers.DatabaseDriver.slickProfile._
import models.baseModels.Model
import scala.reflect.ClassTag
import data.RequiresDatabaseConnection
import data.baseTables.IdTable
import scala.slick.lifted.TableQuery

trait MappedColumnModelID[modelType <: Model[_]] {
  def apply = this.mapper
  
  def mapper: BaseColumnType[modelType]
}

object MappedColumnModelID extends RequiresDatabaseConnection {
  def apply[tableType <: IdTable[idType] with Table[_], modelType <: Model[idType], idType](tableQuery: TableQuery[tableType])(implicit idClass: ClassTag[idType], modelClass: ClassTag[modelType], idcol: BaseColumnType[idType]): BaseColumnType[modelType] = {
    MappedColumnType.base[modelType, idType](
      { model => model.id },
      { id => MappedColumnModelID.db.withSession { implicit session => tableQuery.filter(_.id === id).mapResult(_.asInstanceOf[modelType]).first } })
  }
}