package data.dao

import models.Player
import akka.actor.Actor
import data.RequiresDatabaseConnection
import data.tables.PlayerTable
import data.helpers.DatabaseDriver.slickProfile._
import models.AuthenticationType

object PlayerDAO extends BasicOperations[Long, Player] {
  case class FindPlayerByAuthID(authID: String, authType: AuthenticationType.AuthenticationType) extends Operation[Option[Player]]
}

class PlayerDAO extends Actor with RequiresDatabaseConnection {
  val getQuery = PlayerTable.tableQuery.findBy(_.id)

  def receive = {
    case PlayerDAO.Insert(player) => db.withSession {
      implicit session =>
        sender ! ((PlayerTable.tableQuery returning PlayerTable.tableQuery.map(_.id)) += player)
    }
    case PlayerDAO.Update(player) => db.withSession {
      implicit session =>
        sender ! getQuery(player.id).update(player)
    }
    case PlayerDAO.Delete(key) => db.withSession {
      implicit session =>
        sender ! (getQuery(key).delete > 0)
    }
    case PlayerDAO.Get(key) => db.withSession {
      implicit session =>
        sender ! getQuery(key).firstOption
    }
    case PlayerDAO.Select(keys) => db.withSession {
      implicit session =>
        sender ! PlayerTable.
          tableQuery.
          filter(_.id.inSet(keys)).
          map { player => (player.id, player) }.
          list.
          toMap
    }
    case PlayerDAO.FindPlayerByAuthID(authID, authType) => db.withSession {
      implicit session =>
        sender ! PlayerTable.
          tableQuery.
          filter { player => player.authId === authID && player.authType === authType }.
          firstOption
    }
  }
}