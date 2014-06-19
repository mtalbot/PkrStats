package data.tables

import data.helpers.DatabaseDriver.slickProfile._
import data.helpers.DatabaseDriver.jodaDriver._
import models.Player
import scala.Array
import scala.List
import models.AuthenticationType
import data.helpers.MappedColumnModelID
import data.helpers.MappedColumnStringList.stringMapper
import org.joda.time.DateTime
import data.baseTables.IdTable

object PlayerTable extends SuppliesTableQuery[PlayerTable, Player, Long] with MappedColumnModelID[Player] {
  val tableQuery = TableQuery[PlayerTable]
  
  implicit val mapper = MappedColumnModelID[PlayerTable, Player, Long](this.tableQuery)
}

class PlayerTable(tag: Tag) extends Table[Player](tag, "PLAYERS") with IdTable[Long] {
  val id = column[Long]("ID", O.AutoInc, O.PrimaryKey)
  val name = column[String]("NAME", O.NotNull)
  val nicknames = column[List[String]]("NICKNAMES")
  val authId = column[Option[String]]("AUTH_ID")
  val authToken = column[Option[String]]("AUTH_TOKEN")
  val authTokenExpiry = column[Option[DateTime]]("AUTH_TOKEN_EXPIRY")
  val authType = column[Option[AuthenticationType.AuthenticationType]]("AUTH_TYPE")
  
  def * = (id, name, nicknames, authId, authToken, authTokenExpiry, authType) <> (Player.tupled, Player.unapply)
}