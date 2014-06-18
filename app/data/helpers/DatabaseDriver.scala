package data.helpers

import data.RequiresDatabaseConnection
import scala.slick.driver._
import com.github.tototoshi.slick._

object DatabaseDriver extends RequiresDatabaseConnection {
  val slickDriver = db.driver
  val slickProfile = slickDriver.simple
  val jodaDriver = slickDriver match {
    case H2Driver => H2JodaSupport
    case JdbcDriver => JdbcJodaSupport
    case PostgresDriver => PostgresJodaSupport
  }
}