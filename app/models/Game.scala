package models

import org.joda.time.DateTime

case class Game(
    val id: Long, 
    val series: GameSeries, 
    val hosted: String, 
    val date: DateTime,
    val stake: Double,
    val currency: String
) extends Model[Long]