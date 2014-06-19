package services

import akka.actor.Actor
import scala.io.BufferedSource
import data.dao.Operation
import models.GameSeries
import models.GameResult
import models.Game

object FileUpload {
  case class ParseFile(source: BufferedSource, series: GameSeries) extends Operation[List[(Game, List[GameResult])]]
}

class FileUpload extends Actor {

  def receive = {
    case FileUpload.ParseFile(source, series) => {
      
    }
  }
}