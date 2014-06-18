package data

import play.api.data._
import play.api.db._
import play.api.db.slick.Database
import play.api.Play.current

trait RequiresDatabaseConnection {
	def db = Database()
}