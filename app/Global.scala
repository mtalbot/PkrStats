import data.tables._
import play.api.db._
import play.api.Play.current
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.driver.JdbcDriver.simple._
import play.api.data._
import scala.concurrent.Future
import play.api.mvc.Results._
import play.api.libs.concurrent.Akka
import akka.actor.Props
import play.api.GlobalSettings
import play.api.Application
import play.api.mvc.RequestHeader
import scala.concurrent.ExecutionContext.Implicits.global

object Global extends GlobalSettings {
  override def onError(request: RequestHeader, ex: Throwable) = {
    Future {
      InternalServerError(ex.toString())
    }
  }
  override def onHandlerNotFound(request: RequestHeader) = {
    Future {
      NotFound(request.path)
    }
  }
  override def onBadRequest(request: RequestHeader, error: String) = {
    Future {
      BadRequest("Bad Request: " + error)
    }
  }
  override def beforeStart(app: Application) = {

  }
}