package forms

import org.joda.time.DateTime
import play.api.data._
import play.api.data.Forms._
import models.GameSeries

object GameSeriesForm {
  case class GameSeriesViewModel(name: String, gameTypePath: String)

  val form = Form(
    mapping(
      "name" -> text,
      "type" -> text())(GameSeriesViewModel.apply)(GameSeriesViewModel.unapply))
}