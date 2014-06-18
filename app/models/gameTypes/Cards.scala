package models.gameTypes

object Cards extends GameType {
  object Poker extends GameType {
    object Cash extends GameType {
      case class TexasHoldems() extends GameType {
        val path = Cash.path ++ Seq("TexasHoldems")
      }
      case class Omaha() extends GameType {
        val path = Cash.path ++ Seq("Omaha")
      }

      val path = Poker.path ++ Seq("Cash")
    }
    object Tournament extends GameType { 
      case class TexasHoldems(val startingAmount: Int, val stake: Double, val rebuyAmount: Int, val rebuyStake: Double) extends GameType {
        val path = Tournament.path ++ Seq("TexasHoldems")
      }
      case class Omaha(val startingAmount: Int, val stake: Double, val rebuyAmount: Int, val rebuyStake: Double) {
        val path = Tournament.path ++ Seq("Omaha")
      }

      val path = Poker.path ++ Seq("Tournament")
    }

    val path = Cards.path ++ Seq("Poker")
  }

  object Tricks extends GameType {
    case class Bridge() extends GameType {
      val path = Tricks.path ++ Seq("Bridge")
    }
    
    case class Whist() extends GameType {
      val path = Tricks.path ++ Seq("Whist")
    }
    
    case class Hearts() extends GameType {
      val path = Tricks.path ++ Seq("Hearts")
    }
    
    val path = Cards.path ++ Seq("Tricks")
  }
  
  object RollPlaying extends GameType {
    case class NetRunner(val corporationFaction: String, val runnerFaction: String) extends GameType {
      val path = RollPlaying.path ++ Seq("NetRunner")
    }
    
    val path = Cards.path ++ Seq("RollPlaying")    
  }
  
  val path = Seq("Cards")
}