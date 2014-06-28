package components

import play.api.mvc.Security.AuthenticatedBuilder
import play.api.mvc.Request
import play.api.mvc.RequestHeader
import models.Player
import play.api.mvc._
import akka.actor.ActorSystem
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor.Props
import data.dao._
import play.api.data._
import play.api.data.Forms._
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import play.Routes
import scala.concurrent.Future
import scala.concurrent.CanAwait
import scala.concurrent.Await
import play.api.mvc.Security.AuthenticatedRequest
import org.joda.time.DateTime
import models.baseModels.ChangeLoggedModel

object RequiresAuthentication extends AuthenticatedBuilder(
  implicit request => {
    Security.userFromRequest
  }, onUnautorised => {
    Results.Redirect(controllers.routes.Application.index)
  })

class AuthedRequest[typ](val userobject: Option[Player], val request: Request[typ]) extends AuthenticatedRequest[typ, Option[Player]](userobject, request)

object PotentiallyAuthenticated extends ActionBuilder[AuthedRequest] {
  def invokeBlock[A](request: Request[A], block: (AuthedRequest[A]) => Future[SimpleResult]) = {
    val user = Security.userFromRequest(request)

    block(new AuthedRequest[A](user, request))
  }
}

object RequiresUnautheticated extends ActionBuilder[Request] {
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[SimpleResult]) = {
    val user = Security.userFromRequest(request)

    if (user.isEmpty) {
      block(request)
    } else {
      Future {
        Results.Redirect(controllers.routes.PlayerControler.index)
      }
    }

  }
}

object Security extends DbTimeout {
  val dao = Akka.system.actorOf(Props[PlayerDAO])

  def isAuthenticated(implicit request: Request[_]): Boolean = {
    request match {
      case authedRequest: AuthenticatedRequest[_, _] => authedRequest.user match {
        case usr: Option[_] if usr.isDefined && usr.nonEmpty && usr.get.isInstanceOf[Player] => true
        case _: Player => true
        case _ => false
      }
      case _ => {
        val usr = userFromRequest
        usr.isDefined && usr.nonEmpty
      }
    }
  }

  def userFromRequest(implicit request: RequestHeader): Option[Player] = {
    val usrId = request.session.get("id")

    if (usrId != null && usrId.nonEmpty) {
      Await.result(DAO(dao, PlayerDAO.Get(usrId.get.toLong)), timeoutDuration)
    } else {
      Option[Player](null)
    }
  }

  def userToRequest(implicit result: SimpleResult, player: Player): SimpleResult = {
    result.withSession("id" -> player.id.toString)
  }
  
  def getChangedLoggedFromRequest(existing: Option[ChangeLoggedModel])(implicit request: AuthenticatedRequest[_, Player]): (Player, DateTime, Option[Player], Option[DateTime]) = {
    existing.map{exists =>
      (exists.createdBy, exists.createdOn, Some(request.user), Some(DateTime.now))
    }.getOrElse((request.user, DateTime.now, None, None))
  }
}