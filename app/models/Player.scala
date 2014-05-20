package models

import AuthenticationType._

case class Player(
    val id: Long, 
    val name: String, 
    val nicknames: List[String], 
    val authId: Option[String], 
    val authToken: Option[Array[Byte]], 
    val authType: Option[AuthenticationType]) extends Model[Long]