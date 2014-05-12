package models

case class Player(val id: Long, val name: String, val nicknames: List[String], val email: Option[String], val password: Option[Array[Byte]], val salt: Option[Array[Byte]]) extends Model[Long]