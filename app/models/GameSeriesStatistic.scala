package models

case class GameSeriesStatistic(val positionFrequency: Map[Int, Int], val winnings: Double, val stake: Double, val gameCount: Int, val buyCount: Int, val frequencyOfPlay: Double)