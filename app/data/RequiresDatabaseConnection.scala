package data

import play.api.data._
import play.api.db._
import scala.slick.jdbc.JdbcBackend.Database
import play.api.Play.current

trait RequiresDatabaseConnection {
	def db = Database.forDataSource(DB.getDataSource("default"))
}