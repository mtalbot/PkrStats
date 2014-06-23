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
import play.filters.csrf.CSRFAddToken
import play.filters.csrf.CSRFCheck
import play.api.templates.Html
import components.JsonSerializer
import models.GameSeriesStatistic
import play.api.templates.HtmlFormat
import forms.GameSeriesForm
import models.gameTypes.Games
import models.gameTypes.Cards
import scala.io.Source
import services.FileUpload
import data.dao.GameDAO
import models.Game
import models.GameResult
import models.Player

object GameSeries extends ContentNegotiatedControler with DbTimeout {

  val gameSeriesDAO = Akka.system.actorOf(Props[GameSeriesDAO])
  val gameDAO = Akka.system.actorOf(Props[GameDAO])
  val fileUploadSrv = Akka.system.actorOf(Props[FileUpload])

  def newSeries = CSRFAddToken {
    RequiresAuthentication { implicit request =>
      Ok(views.html.editViews.gameSeries(GameSeriesForm.form))
    }
  }

  def view(id: Long) = CSRFAddToken {
    RequiresAuthentication.async { implicit request =>
      val op = GameSeriesDAO.Get(id)
      val op2 = GameSeriesDAO.GetStatistics(id, None)
      val series = (gameSeriesDAO ? op).map(op.getResult(_))
      val stats = (gameSeriesDAO ? op2).map(op2.getResult(_))

      implicit val serialiser = new JsonSerializer[(models.GameSeries, Map[Player, GameSeriesStatistic], Boolean)]

      for {
        seriesValue <- series
        statsValue <- stats
      } yield seriesValue.map { seriesVal =>
        renderPartial[(models.GameSeries, Map[Player, GameSeriesStatistic], Boolean), HtmlFormat.Appendable](
          Ok, "", Html.empty, views.html.dataViews.gameSeries.apply _, (seriesVal, statsValue, true))
      }.getOrElse(NotFound(""))
    }
  }

  def create() = CSRFCheck {
    RequiresAuthentication.async { implicit request =>
      GameSeriesForm.form.bindFromRequest.fold({ erroredForm =>
        Future {
          BadRequest(views.html.editViews.gameSeries(erroredForm))
        }
      }, { seriesViewModel =>
        val log = Security.getChangedLoggedFromRequest(None)
        val op = GameSeriesDAO.Insert(models.GameSeries(-1, seriesViewModel.name, None, log._1, log._2, log._3, log._4))

        val result = (gameSeriesDAO ? op).
          map(op.getResult(_)).
          map { id =>
            Redirect(routes.GameSeries.view(id))
          }

        result
      })
    }
  }

  def save(id: Option[Long]) = CSRFCheck {
    RequiresAuthentication {
      Ok("hello world")
    }
  }

  def delete(id: Long) = CSRFCheck {
    RequiresAuthentication {
      Ok("hello world")
    }
  }

  def upload(id: Long) = CSRFCheck {
    RequiresAuthentication.async(parse.multipartFormData) { implicit request =>

      val upload = Future.sequence(request.body.files.map { file =>

        val op = GameSeriesDAO.Get(id)

        val source = Future { Source.fromFile(file.ref.file).getLines.toSeq }

        val series = (gameSeriesDAO ? op).
          map { result => op.getResult(result) }

        val op2 = for {
          seriesResult <- series
          sourceResult <- source
        } yield seriesResult.map(seriesData => FileUpload.ParseFile(sourceResult, seriesData))

        op2.flatMap {
          _.map { opResult =>
            (fileUploadSrv ? opResult).
              map(opResult.getResult(_))
             }.get
        }
      })
      
      upload.onFailure {
        case ex: Throwable => throw ex
      }
      
      implicit val serialiser = new JsonSerializer[Seq[(Seq[Player], Seq[(Game, Seq[GameResult])])]]
      
      upload.map { 
        case Seq() => BadRequest("No files in upload")
        case value => renderPartial(Ok, "Please confirm upload", Html.empty, views.html.editViews.gameSeriesUploadConfirm.apply _, value) 
      }
    }
  }
}