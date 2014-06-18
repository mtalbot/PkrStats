package data.dao

import akka.actor.Actor
import models._
import data.tables._
import data.helpers.DatabaseDriver.slickProfile._
import data.RequiresDatabaseConnection
import data.tables.GameTable.{ mapper => gameMapper }
import data.tables.PlayerTable.{ mapper => playerMapper }

object GameDAO extends BasicOperations[Long, (Game, List[GameResult])] {
  case class GetAllGamesForPlayer(player: Player) extends Operation[List[Game]]
}

class GameDAO extends Actor with RequiresDatabaseConnection {
  val getQuery = GameTable.tableQuery.findBy(_.id)
  val getResultsQuery = GameResultTable.tableQuery.findBy(_.game.asColumnOf[Long])

  def receive = {
    case GameDAO.Insert(obj) => this.db.withSession { implicit session =>
      val game = (GameTable.tableQuery returning GameTable.tableQuery.map(f => f)) += obj._1

      val results = obj._2.map {
        row =>
          GameResult(row.id, game, row.player, row.position, row.stake, row.score)
      }

      GameResultTable.tableQuery ++= results

      sender ! game.id
    }
    case GameDAO.Update(obj) => this.db.withSession { implicit session =>
      val objCount = getQuery(obj._1.id).update(obj._1)

      val newIds = obj._2.map(_.id)

      val ids = getResultsQuery(obj._1.id).mapResult(_.id).list
      val toDelete = GameResultTable.tableQuery.filterNot(_.id.inSet(newIds)).delete //Remove old entries

      obj._2.filter { res => ids.contains(res.id) }.foreach { res =>
        val q = GameResultTable.tableQuery.findBy(_.id)
        q(res.id).update(res)
      }

      val resCount = (GameResultTable.tableQuery ++= obj._2.filterNot { res => ids.contains(res.id) })

      sender ! resCount.getOrElse(0) + objCount
    }
    case GameDAO.Delete(key) => this.db.withSession { implicit session =>
      getResultsQuery(key).delete
      sender ! (getQuery(key).delete > 0)
    }
    case GameDAO.Get(key) => this.db.withSession { implicit session =>
      sender ! (getQuery(key).firstOption, getResultsQuery(key).list)
    }
    case GameDAO.Select(keys) => this.db.withSession { implicit session =>
      sender ! GameResultTable.
        tableQuery.
        filter(_.game.asColumnOf[Long].inSet(keys)).
        groupBy(_.game).
        list.
        map(grp => (grp._1.id, (grp._1, grp._2.asInstanceOf[GameResult])))
    }

  }
}