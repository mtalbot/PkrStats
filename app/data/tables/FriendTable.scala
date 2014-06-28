package data.tables

import data.helpers.DatabaseDriver.slickProfile._
import data.baseTables.IdTable
import models.Player
import data.tables.PlayerTable.{ mapper => playerMapper }
import data.helpers.MappedColumnModelID
import models.Friend

object FriendTable extends SuppliesTableQuery[FriendTable, Friend, Long] {
  val tableQuery = TableQuery[FriendTable]
}

class FriendTable(tag: Tag) extends Table[Friend](tag, "FRIENDS") with IdTable[Long] {
  val id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  val player = column[Player]("PLAYER", O.NotNull)
  val friend = column[Player]("FRIEND", O.NotNull)
  
  def * = (id, player, friend) <> (Friend.tupled, Friend.unapply)
}