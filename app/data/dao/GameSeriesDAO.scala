package data.dao

import akka.actor.Actor
import data.RequiresDatabaseConnection
import data.tables.GameSeriesTable
import data.tables.GameTable
import data.helpers.DatabaseDriver.slickProfile._
import models._
import data.tables.GameTable.{ mapper => gameMapper }
import data.tables.GameSeriesTable.{ mapper => gameSeriesMapper }
import data.tables.PlayerTable.{ mapper => playerMapper }
import data.tables.GameResultTable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.slick.ast.JoinType

object GameSeriesDAO extends BasicOperations[Long, GameSeries] {
  case class GetStatistics(key: Long, normalize: Option[Int]) extends Operation[Map[Player, GameSeriesStatistic]]
  case class GetForPlayer(player: Player) extends Operation[List[GameSeries]]
}

class GameSeriesDAO extends Actor with RequiresDatabaseConnection {

  val getQuery = GameSeriesTable.tableQuery.findBy(_.id)

  def receive = {
    case GameSeriesDAO.Insert(obj) => db.withSession { implicit session =>
      sender ! ((GameSeriesTable.tableQuery returning GameSeriesTable.tableQuery.map(_.id)) += obj)
    }
    case GameSeriesDAO.Update(obj) => db.withSession { implicit session =>
      sender ! getQuery(obj.id).update(obj)
    }
    case GameSeriesDAO.Delete(key) => db.withSession { implicit session =>
      sender ! (getQuery(key).delete > 0)
    }
    case GameSeriesDAO.Get(key) => db.withSession { implicit session =>
      sender ! getQuery(key).firstOption
    }
    case GameSeriesDAO.Select(keys) => db.withSession { implicit session =>
      sender ! GameSeriesTable.
        tableQuery.
        filter(_.id.inSet(keys)).
        map { series => (series.id, series) }.
        toMap
    }
    case GameSeriesDAO.GetForPlayer(player) => db.withSession { implicit session =>
      sender ! GameSeriesTable.tableQuery.filter(_.id.in {
        GameTable.tableQuery.filter(_.id.in {
          GameResultTable.
            tableQuery.
            filter(_.player === player).map(_.game.asColumnOf[Long])
        }).map(_.series.asColumnOf[Long])
      }).list
    }
    case GameSeriesDAO.GetStatistics(key, normalize) => {
      val theSender = sender
      val gamesQuery = GameTable.
        tableQuery.
        filter(_.series.asColumnOf[Long] === key).
        map(_.id)

      val positionGroup: (data.tables.GameResultTable, Column[Int]) => Column[Int] = normalize match {
        case None => { (group: data.tables.GameResultTable, gameSize: Column[Int]) => group.position }
        case Some(normal) => { (group: data.tables.GameResultTable, gameSize: Column[Int]) =>
          ((group.position.asColumnOf[Double] / gameSize.asColumnOf[Double]) * normal.asInstanceOf[Double]).floor.asColumnOf[Int]
        }
      }

      val gameresults = GameResultTable.
        tableQuery.
        filter(_.game.asColumnOf[Long].in(gamesQuery))

      val statsQuery = Future {
        db.withSession { implicit session =>
          gameresults.
            groupBy(_.game).
            map { result => (result._1, result._2.length) }.
            innerJoin(gameresults).
            on { (left, right) => left._1 === right.game }.
            map { row => (row._2.player, positionGroup(row._2, row._1._2)) }.
            groupBy { row => (row._1, row._2) }.
            map { grp => (grp._1._1, (grp._1._2, grp._2.length)) }.
            list.
            groupBy(_._1).
            map { grp => (grp._1, grp._2.map(_._2).toMap) }
        }
      }

      val playerQuery = Future {
        db.withSession { implicit session =>
          gameresults.
            innerJoin(GameTable.tableQuery).
            on { (result, game) => result.game.asColumnOf[Long] === game.id }.
            map { pair => (pair._1.player, (pair._1.score.getOrElse(0), pair._1.stake.getOrElse(0), pair._2)) }.
            groupBy(_._1).
            map { grp => (grp._1, (grp._2.map(_._2._1).sum.getOrElse(0), grp._2.map(_._2._2).sum.getOrElse(0), grp._2.map(_._2._3).countDistinct, grp._2.length)) }.
            toMap
        }
      }

      val countQuery = Future { db.withSession { implicit session => gameresults.length.run } }

      val result = for (
        gameCount <- countQuery;
        stats <- statsQuery;
        aggregates <- playerQuery
      ) yield (
        for (
          stat <- stats;
          aggregate <- aggregates if (stat._1 == aggregate._1)
        ) yield (stat._1, (GameSeriesStatistic(stat._2, aggregate._2._1, aggregate._2._2, aggregate._2._3, aggregate._2._4, aggregate._2._3 / gameCount.toDouble))))

      result.onSuccess {
        case res => theSender ! res
      }

      result.onFailure{
        case ex: Throwable => throw ex
      }
    }
  }
}