package controllers

import play.api.mvc._
import components.ContentNegotiatedControler
import components.DbTimeout
import components.PotentiallyAuthenticated
import org.joda.time.DateTime
import components.Security
import scala.concurrent.Future
import akka.actor.ActorSystem
import play.api.libs.concurrent.Akka
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorRef
import akka.actor.Props
import data.dao.PlayerDAO
import components.JsonSupport
import java.io.StringReader
import play.api.libs.json._
import components.RequiresAuthentication
import data.dao.GameSeriesDAO
import play.Logger
import data.dao.GameDAO
import data.dao.DAO

object PlayerControler extends ContentNegotiatedControler with DbTimeout {

  val playerDAO = Akka.system.actorOf(Props[PlayerDAO])
  val gameSeriesDAO = Akka.system.actorOf(Props[GameSeriesDAO])
  val gameDAO = Akka.system.actorOf(Props[GameDAO])

  def index = RequiresAuthentication.async { implicit request =>
    val result = DAO(gameSeriesDAO, GameSeriesDAO.GetForPlayer(request.user)).
      flatMap { result =>
        Future.sequence(result.
          map { series =>
            for {
              stats <- DAO(gameSeriesDAO, GameSeriesDAO.GetStatistics(series.id, None))
              last <- DAO(gameDAO, GameDAO.GetLatest(series.id))
            } yield last.map { lastGame => (series, (stats, lastGame)) }
          })
      }.map(_.filter(_.isDefined).map(_.get)).map { result =>
        Ok(views.html.player(request.user, result.toMap))
      }

    result.onSuccess {
      case res => Logger.info("Found Records")
    }

    result
  }

}