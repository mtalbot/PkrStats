package forms

import org.joda.time.DateTime
import play.api.data._
import play.api.data.Forms._
import forms.PlayerForm.PlayerViewModel
import forms.GameForm._
import org.joda.time.DateTimeZone

object GameSeriesUploadForm {
  case class GameSeriesUpload(players: Seq[PlayerViewModel], games: Seq[GameViewModel])

  val form = Form(
    mapping(
      "players" -> seq(mapping(
        "id" -> longNumber,
        "name" -> text,
        "nicknames" -> seq(text))(PlayerViewModel.apply)(PlayerViewModel.unapply)),
      "games" -> seq(mapping(
        "gameTypePath" -> text,
        "hosted" -> text,
        "playedOn" -> jodaDate("y/M/d", DateTimeZone.UTC),
        "results" -> seq(mapping(
          "position" -> number,
          "player" -> longNumber,
          "name" -> text)(GameResultViewModel.apply)(GameResultViewModel.unapply)))(GameViewModel.apply)(GameViewModel.unapply)))(GameSeriesUpload.apply)(GameSeriesUpload.unapply))
}