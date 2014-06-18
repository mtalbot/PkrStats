package models.gameTypes

trait GameType {
	def path: Seq[String]
	
	override def toString = path.foldRight("")(_ + "." + _)
}