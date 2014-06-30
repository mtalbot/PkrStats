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
import forms.GameSeriesUploadForm
import forms.GameSeriesUploadForm.GameSeriesUpload
import models.GameSeries
import forms.PlayerForm
import forms.GameForm
import models.Player
import models.GameResult
import data.dao.DAO
import scala.util.Success
import scala.util.Success
import data.dao.FriendDAO
import models.Friend
import components.JsonSerializer

object GameSeriesControler extends ContentNegotiatedControler with DbTimeout {

  val gameSeriesDAO = Akka.system.actorOf(Props[GameSeriesDAO])
  val gameDAO = Akka.system.actorOf(Props[GameDAO])
  val playerDAO = Akka.system.actorOf(Props[PlayerDAO])
  val friendDAO = Akka.system.actorOf(Props[FriendDAO])
  val fileUploadSrv = Akka.system.actorOf(Props[FileUpload])

  implicit val serialiserForUpload = new JsonSerializer[(GameSeries, Seq[Form[GameSeriesUploadForm.GameSeriesUpload]])]

  def newSeries = CSRFAddToken {
    RequiresAuthentication { implicit request =>
      Ok(views.html.editViews.gameSeries(GameSeriesForm.form))
    }
  }

  def view(id: Long, gameCountFilter: Option[Int], normalized: Option[Int]) = CSRFAddToken {
    RequiresAuthentication.async { implicit request =>
      implicit val serialiser = new JsonSerializer[(models.GameSeries, Map[Player, GameSeriesStatistic], (Game, List[GameResult]), Boolean)]

      for {
        seriesValue <- DAO(gameSeriesDAO, GameSeriesDAO.Get(id))
        statsValue <- DAO(gameSeriesDAO, GameSeriesDAO.GetStatistics(id, normalized)).map { stats =>
          stats.filter { stat =>
            gameCountFilter.fold(true)(stat._2.gameCount > _)
          }
        }
        lastGameValue <- DAO(gameDAO, GameDAO.GetLatest(id))
      } yield seriesValue.flatMap { seriesVal =>
        lastGameValue.map { lastGame =>
          renderPartial(Ok, "", Html.empty, views.html.dataViews.gameSeries.apply _, (seriesVal, statsValue, lastGame, true))
        }
      }.getOrElse { NotFound("") }
    }
  }

  def viewAllGames(id: Long) = RequiresAuthentication.async { implicit request =>
    implicit val serialiser = new JsonSerializer[List[(Game, List[GameResult])]]

    DAO(gameDAO, GameDAO.GetAllGames(id)).map { game =>
      renderPartial(Ok, "", Html.empty, views.html.dataViews.gameList.apply _, game)
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

        val result = DAO(gameSeriesDAO, GameSeriesDAO.Insert(models.GameSeries(-1, seriesViewModel.name, None, log._1, log._2, log._3, log._4))).
          map { id =>
            Redirect(routes.GameSeriesControler.view(id, None, None))
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

  def completeUpload(id: Long) = CSRFCheck {
    RequiresAuthentication.async(parse.urlFormEncoded(1024 * 1024 * 20)) { implicit request =>
      DAO(gameSeriesDAO, GameSeriesDAO.Get(id)).flatMap { gameSeries =>
        GameSeriesUploadForm.form.bindFromRequest.fold(
          { formWithErrors =>
            Future { renderPartial(BadRequest, "Please confirm upload", Html.empty, views.html.editViews.gameSeriesUploadConfirm.apply _, (gameSeries.get, Seq(formWithErrors))) }
          },
          { gameSeriesUpload =>
            Future.sequence(gameSeriesUpload.
              players.
              filter(_.id < 0).
              map { player =>
                DAO(playerDAO, PlayerDAO.Insert(Player(player.id, player.name, player.nicknames.toList, None, None, None, None))).
                  andThen {
                    case Success(id) => DAO(playerDAO, PlayerDAO.Get(id)).map(_.map { newPlayer => DAO(friendDAO, FriendDAO.Insert(Friend(-1, request.user, newPlayer))) })
                  }.
                  map { result => (player.id, result) }
              }).
              map { newPlayers =>
                (newPlayers ++ gameSeriesUpload.players.filter(_.id >= 0).map { player => (player.id, player.id) }).toMap
              }.flatMap { allPlayers =>
                Future.sequence(allPlayers.
                  map { player =>
                    DAO(playerDAO, PlayerDAO.Get(player._2)).
                      map { result => (player._1, result) }
                  })
              }.
              map { players =>
                players.toMap
              }.
              flatMap { playersLookup =>
                Future.sequence(gameSeriesUpload.
                  games.
                  map { game =>
                    val gameentry = Game(-1, gameSeries.get, game.hosted, game.playedOn, Cards)
                    val gameresults = for {
                      gameResult <- game.results
                      player <- playersLookup(gameResult.player)
                    } yield GameResult(-1, gameentry, player, gameResult.position, None, None)

                    DAO(gameDAO, GameDAO.Insert((gameentry, gameresults.toList)))
                  })
              }.map { result =>
                Redirect(routes.GameSeriesControler.view(id, None, None))
              }
          })
      }
    }
  }

  def upload(id: Long) = CSRFCheck {
    RequiresAuthentication.async(parse.maxLength(1024 * 1024 * 20, parse.multipartFormData)) { implicit request =>
      val series = DAO(gameSeriesDAO, GameSeriesDAO.Get(id))

      request.body.fold({ overSize => Future { EntityTooLarge("Request too large") } }, { body =>
        val upload = Future.sequence(body.files.map { file =>

          val source = Future { Source.fromFile(file.ref.file).getLines.toSeq }

          val op2 = for {
            seriesResult <- series
            sourceResult <- source
          } yield (seriesResult, sourceResult)

          op2.flatMap { result =>
            result._1.fold {
              Future(Option.empty[Form[GameSeriesUploadForm.GameSeriesUpload]])
            } { result2 =>
              DAO(fileUploadSrv, FileUpload.ParseFile(result._2, result2)).
                map { result =>
                  Some(GameSeriesUploadForm.form.fill(GameSeriesUploadForm.GameSeriesUpload(
                    result._1.map { player =>
                      PlayerForm.PlayerViewModel(player.id, player.name, player.nicknames)
                    },
                    result._2.map { game =>
                      GameForm.GameViewModel(game._1.gametype.toString, game._1.hosted, game._1.date, game._2.map { result =>
                        GameForm.GameResultViewModel(result.position, result.player.id, result.player.name)
                      })
                    })))
                }
            }
          }
        }).map { uploads => uploads.filter(_.isDefined).map(_.get) }

        upload.onFailure {
          case ex: Throwable => throw ex
        }

        for {
          uploads <- upload
          seriesData <- series
        } yield renderPartial(Ok, "Please confirm upload", Html.empty, views.html.editViews.gameSeriesUploadConfirm.apply _, (seriesData.get, uploads))
      })
    }
  }
}