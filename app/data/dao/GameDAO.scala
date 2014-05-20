package data.dao

import akka.actor.Actor
import models._
import data.tables._
import akka.actor.Actor
import play.api.db._
import play.api.Play.current
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.driver.JdbcDriver.simple._
import play.api.data._
import data.RequiresDatabaseConnection
import data.tables.GameTable.{mapper => gameMapper}
import data.tables.PlayerTable.{mapper => playerMapper}

object GameDAO extends BasicOperations[Long, (Game, List[GameResult])]

class GameDAO extends Actor with RequiresDatabaseConnection {
  def receive = {
    case GameDAO.Insert(obj) => {
      this.db.withSession { implicit session =>
        val game = (GameTable.tableQuery returning GameTable.tableQuery.map(f => f)) += obj._1

        val results = obj._2.map {
          row =>
            GameResult(row.id, game, row.position, row.player)
        }

        GameResultTable.tableQuery ++= results

        sender ! game.id
      }
    }
    case GameDAO.Update(obj) =>
    case GameDAO.Delete(key) => {
      this.db.withSession { implicit session =>
        val q = GameTable.tableQuery.findBy(_.id)
        
        
        val q2 = GameResultTable.tableQuery.findBy(_.game.asColumnOf[Long])

        q(key).delete
        q2(key).delete

        sender ! true
      }
    }
    case GameDAO.Get(key) => {
      this.db.withSession { implicit session =>
        val q = GameTable.tableQuery.findBy(_.id)
        val q2 = GameResultTable.tableQuery.findBy(_.game.asColumnOf[Long])
        val res = (q(key).firstOption, q2(key).list)
        sender ! res
      }
    }
    case GameDAO.Select(keys) => {
      this.db.withSession { implicit session =>
        val q = GameTable.tableQuery.filter { row => keys.contains(row.id) }
        val q2 = GameResultTable.tableQuery.filter { row => keys.contains(row.game) }

        val games = q.list
        val results = q2.list.groupBy(_.game).toMap

        val res = games.map(row => (row.id, (row, results(row))))

        sender ! res
      }
    }
  }
}