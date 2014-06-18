package models

import AuthenticationType._
import org.joda.time.DateTime

case class Player(
    val id: Long, 
    val name: String, 
    val nicknames: List[String], 
    val authId: Option[String], 
    val authToken: Option[String], 
    val authTokenExpiry: Option[DateTime],
    val authType: Option[AuthenticationType]) extends Model[Long]