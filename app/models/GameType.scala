package models

import data.helpers.MappedColumnEnumeration

object GameType extends MappedColumnEnumeration {
	type GameType = Value
	val PokerTexasHoldems, PokerOmaha, Poker3CardStud, Poker5CardStud = Value
}