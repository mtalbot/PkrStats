package controllers

import components.ContentNegotiatedControler
import components.DbTimeout
import components.RequiresAuthentication
import play.api.libs.concurrent.Akka
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current
import akka.actor.Props
import data.dao.GameDAO
import data.dao.DAO
import play.api.templates.Html
import components.JsonSupport
import models.GameResult
import models.Game
import components.JsonSerializer

object GameController extends ContentNegotiatedControler with DbTimeout {

  val gameDAO = Akka.system.actorOf(Props[GameDAO])

  def view(seriesId: Long, gameId: Long) = RequiresAuthentication.async { implicit request =>
    DAO(gameDAO, GameDAO.Get(gameId)).map(_.filter(_._1.series.id == seriesId)).map(_.fold(NotFound("")) { game =>
      implicit val serialiser = new JsonSerializer[(Game, List[GameResult])]

      renderPartial(Ok, game._1.toString, Html.empty, views.html.dataViews.game.apply _, game)
    })
  }
}