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

object Player extends ContentNegotiatedControler with DbTimeout {

  val playerDAO = Akka.system.actorOf(Props[PlayerDAO])
  val gameSeriesDAO = Akka.system.actorOf(Props[GameSeriesDAO])

  def index = RequiresAuthentication.async { implicit request =>
    val op = GameSeriesDAO.GetForPlayer(request.user)

    val result = (gameSeriesDAO ? op).
      flatMap { result =>
        Future.sequence(op.
          getResult(result).
          map { series =>
            (series, GameSeriesDAO.GetStatistics(series.id, None))
          }.map { opp =>
            (gameSeriesDAO ? opp._2).map { res => (opp._1, opp._2.getResult(res)) }
          })
      }.map { result =>
        Ok(views.html.player(request.user, result.toMap))
      }

     result.onSuccess{
         case res => Logger.info("Found Records")
     }
      
    result
  }

}