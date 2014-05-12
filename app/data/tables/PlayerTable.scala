package data.tables

import scala.slick.driver.JdbcDriver.simple._
import models.Player

object PlayerTable extends SuppliesTableQuery[PlayerTable, Player, Long] with MappedColumnModelID[Player] {
  val tableQuery = TableQuery[PlayerTable]
  
  implicit val mapper = MappedColumnModelID[Player, Long](this)
  
  override def getById(id: Long)(implicit session: Session): Option[Player] = tableQuery.findBy(_.id).apply(id).firstOption
}

class PlayerTable(tag: Tag) extends Table[Player](tag, "PLAYERS") {

  implicit val stringListMapper = MappedColumnType.base[List[String], String](
    { list =>
      list.map(str => str.replace(":", "::")).fold("")((left, right) => (left, right) match {
        case (left, right) if left == null || left.isEmpty() => right
        case (left, right) if right == null || right.isEmpty() => left
        case (left, right) if (right == null || right.isEmpty()) && (left == null || left.isEmpty()) => ""
        case _ => left + ":" + right
      })
    },
    { str => List.fromArray(str.split("(?<!:):")).map(str => str.replace("::", ":")) }) // Needs a back reference to stop it matching escaped colons

  val id = column[Long]("ID", O.AutoInc, O.PrimaryKey)
  val name = column[String]("NAME", O.NotNull)
  val nicknames = column[List[String]]("NICKNAMES")
  val email = column[Option[String]]("EMAIL")
  val password = column[Option[Array[Byte]]]("PASSWORD")
  val salt = column[Option[Array[Byte]]]("PASSWORD")
  
  def * = (id, name, nicknames, email, password, salt) <> (Player.tupled, Player.unapply)
}