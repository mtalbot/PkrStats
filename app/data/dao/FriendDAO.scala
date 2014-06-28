package data.dao

import data.RequiresDatabaseConnection
import akka.actor.Actor
import models.Player
import data.tables.FriendTable
import data.helpers.DatabaseDriver.slickProfile._
import data.tables.PlayerTable.mapper
import models.Friend

object FriendDAO extends BasicOperations[Long, Friend] {
  case class GetPlayersFriends(player: Player) extends Operation[Map[Long, Player]]

  val getQuery = FriendTable.tableQuery.findBy(_.id)
}

class FriendDAO extends Actor with RequiresDatabaseConnection {

  def receive = {
    case FriendDAO.Get(id) => db.withSession { implicit session =>
      sender ! FriendDAO.getQuery(id).firstOption
    }
    case FriendDAO.Insert(obj) => db.withSession { implicit session =>
      sender ! ((FriendTable.tableQuery returning FriendTable.tableQuery.map(_.id)) += obj)
    }
    case FriendDAO.Delete(id) => db.withSession { implicit session =>
      sender ! (FriendDAO.getQuery(id).delete > 0)
    }
    case FriendDAO.Update(obj) => db.withSession { implicit session =>
      sender ! (FriendDAO.getQuery(obj.id).update(obj))
    }
    case FriendDAO.Select(keys) => db.withSession { implicit session =>
      sender ! (FriendTable.tableQuery.filter(_.id inSet keys).list)
    }
    case FriendDAO.GetPlayersFriends(player) => db.withSession { implicit session =>
      sender ! (FriendTable.
        tableQuery.
        filter(_.player.asColumnOf[Long] === player.id).
        map { friend => (friend.id, friend.friend) }.
        toMap)
    }
  }
}