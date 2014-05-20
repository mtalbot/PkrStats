package data.tables

import scala.slick.driver.JdbcDriver.simple._
import models.Player
import scala.Array
import scala.List
import models.AuthenticationType
import data.helpers.MappedColumnModelID

object PlayerTable extends SuppliesTableQuery[PlayerTable, Player, Long] with MappedColumnModelID[Player] {
  val tableQuery = TableQuery[PlayerTable]
  
  implicit val mapper = MappedColumnModelID[PlayerTable, Player, Long](this.tableQuery)
}

class PlayerTable(tag: Tag) extends Table[Player](tag, "PLAYERS") with IdTable[Long] {

  implicit val stringListMapper = MappedColumnType.base[List[String], String](
    { list =>
      list.map(str => str.replace(":", "::")).fold("")((left, right) => (left, right) match {
        case (left, right) if left == null || left.isEmpty() => right
        case (left, right) if right == null || right.isEmpty() => left
        case (left, right) if (right == null || right.isEmpty()) && (left == null || left.isEmpty()) => ""
        case _ => left + ":" + right
      })
    },
    { str => (str.split("(?<!:):(?!:)")).map(str => str.replace("::", ":")).toList })

  val id = column[Long]("ID", O.AutoInc, O.PrimaryKey)
  val name = column[String]("NAME", O.NotNull)
  val nicknames = column[List[String]]("NICKNAMES")
  val authId = column[Option[String]]("AUTH_ID")
  val authToken = column[Option[Array[Byte]]]("AUTH_TOKEN")
  val authType = column[Option[AuthenticationType.AuthenticationType]]("AUTH_TYPE")
  
  def * = (id, name, nicknames, authId, authToken, authType) <> (Player.tupled, Player.unapply)
}