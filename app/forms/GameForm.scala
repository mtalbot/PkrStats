package forms

import org.joda.time.DateTime
import play.api.data._
import play.api.data.Forms._

object GameForm {
  case class GameResultViewModel(position: Int, player: Long, name: String)
  case class GameViewModel(
    gameTypePath: String,
    hosted: String,
    playedOn: DateTime,
    results: Seq[GameResultViewModel])
}