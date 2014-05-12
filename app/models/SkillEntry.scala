package models

case class SkillEntry(val id: Long, val game: GameResult, val mean: Double, val stddev: Double) extends Model[Long]