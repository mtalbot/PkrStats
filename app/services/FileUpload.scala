package services

import akka.actor.Actor
import scala.io.BufferedSource
import data.dao.Operation
import models.GameSeries
import models.GameResult
import models.Game
import models.Player
import org.joda.time.DateTime
import models.Game
import models.gameTypes.Cards
import models.GameResult
import scala.reflect.ClassTag
import models.gameTypes.GameType

object FileUpload {
  case class ParseFile(source: Seq[String], series: GameSeries) extends Operation[(Seq[Player], Seq[(Game, Seq[GameResult])])]
}

class FileUpload extends Actor {
  private def partition[elementType](data: Seq[elementType], partitionFunction: Seq[elementType] => Boolean)(implicit tag: ClassTag[elementType]): Seq[Seq[elementType]] = {
    val res = data.
      sliding(2).
      span(partitionFunction)

    val left = res._1.map(_.head).toSeq
    val right = res._2.map(_.head).toSeq

    val rightSplit = if (right.size > 0) { partition(right.drop(2), partitionFunction) } else { Seq() }

    if (rightSplit.size > 0) {
      Seq(left) ++ rightSplit
    } else {
      Seq(left)
    }
  }

  def receive = {
    case FileUpload.ParseFile(source, series) => {
      var theSender = sender

      val doubleNewLineSplit: Seq[(String, Int)] => Boolean = {
        case window if window.size == 1 => true
        case window if window.size > 1 => !(window.head._1.trim().isEmpty() && window.last._1.trim().isEmpty())
      }

      val data = partition(source.zipWithIndex, doubleNewLineSplit).map { value => (value, value.head._2) }

      val truncate: String => String = input => input.
          dropWhile(_ == '=').
          toLowerCase.
          trim 
      
      val players = data.
        map(_._1.sortBy(_._2).drop(2)).
        flatMap(a => a.map{ b => truncate(b._1)}).
        distinct.
        zipWithIndex.
        map { playerName =>
          (playerName._1, Player(playerName._2 * -1, playerName._1, List(), None, None, None, None))
        }.
        toMap

      val games = data.map { game =>
        (game._2, (game._1.head._1.split(" ").head.split("/").map(_.toInt), game._1.drop(1).head._1.split("\t"), game._1.drop(2).zipWithIndex.map(_.swap).map { player =>
          (player._1, player._2._1.startsWith("="), players(truncate(player._2._1)))
        }))
      }

      val sortedGames = games.
        seq.
        toSeq.
        sortBy(_._1)

      val year = DateTime.now.getYear

      val yearSplit: Seq[(Int, (Array[Int], Array[String], Seq[(Int, Boolean, Player)]))] => Boolean = {
        case window if window.size < 2 => true
        case window if window.size >= 2 => window.head._2._1.last <= window.last._2._1.last
      }

      val gameType = series.defaultGameType.getOrElse(Cards)

      val gameObjs = partition(sortedGames, yearSplit).
        reverse.
        zipWithIndex.
        flatMap { data =>
          data._1.map { game =>
            val date = new DateTime(year - data._2, game._2._1(1).toInt, game._2._1(0).toInt, 0, 0)
            val gameObj = Game(game._1 * -1, series, game._2._2.head, date, gameType)

            (gameObj, game._2._3.map { entry =>
              GameResult(-1, gameObj, entry._3, entry._1, None, None)
            })
          }
        }.
        sortBy(_._1.date.toDate()).
        reverse

      val result = (players.values.toSeq.sortBy(_.name), gameObjs)
      theSender ! result
    }
  }
}