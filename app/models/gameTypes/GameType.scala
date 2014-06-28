package models.gameTypes

import scala.reflect.ClassTag
import org.reflections.Reflections
import collection.JavaConversions._

object GameTypes {
  val ref = new Reflections("models.gameTypes")
  
  val gameTypes = ref.getSubTypesOf(classOf[GameType]).toSet
  
  def toString(gameType: GameType): String = {
    gameType.toString
  }
  
  def fromString(gameType: String): GameType = {
    Cards
  }
}

trait GameType {
	def path: Seq[String]
	
	override def toString = path.foldRight("")(_ + "." + _)
}