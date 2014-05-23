package data.dao

import data.RequiresDatabaseConnection
import akka.actor.Actor
import models.SkillEntry
import data.tables.SkillEntryTable
import scala.slick.driver.JdbcDriver.simple._

object SkillDAO extends BasicOperations[Long, SkillEntry]

class SkillDAO extends Actor with RequiresDatabaseConnection {
  val getQuery = SkillEntryTable.tableQuery.findBy(_.id)

  def receive = {
    case SkillDAO.Insert(obj) => db.withSession {
      implicit session =>
        sender ! ((SkillEntryTable.tableQuery returning SkillEntryTable.tableQuery.map(_.id)) += obj)
    }
    case SkillDAO.Update(obj) => db.withSession {
      implicit session =>
        sender ! getQuery(obj.id).update(obj)
    }
    case SkillDAO.Delete(key) => db.withSession {
      implicit session =>
        sender ! (getQuery(key).delete > 0)
    }
    case SkillDAO.Get(key) => db.withSession {
      implicit session =>
        sender ! getQuery(key).firstOption
    }
    case SkillDAO.Select(keys) => db.withSession {
      implicit session =>
        sender ! SkillEntryTable.
          tableQuery.
          filter(_.id.inSet(keys)).
          list.
          map { entry => (entry.id, entry) }.
          toMap
    }
  }
}