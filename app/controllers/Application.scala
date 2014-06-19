package controllers

import play.api.mvc._
import components.ContentNegotiatedControler
import components.DbTimeout
import components.PotentiallyAuthenticated
import org.joda.time.DateTime
import components.Security
import scala.concurrent.Future
import akka.actor.ActorSystem
import play.api.libs.concurrent.Akka
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorRef
import akka.actor.Props
import data.dao.PlayerDAO
import components.JsonSupport
import java.io.StringReader
import models.AuthenticationType
import play.api.libs.json._
import forms.AuthenticateForm
import components.RequiresUnautheticated
import play.filters.csrf._
import services.Login
import akka.dispatch.OnSuccess

object Application extends ContentNegotiatedControler with DbTimeout {

  val loginSrv: ActorRef = Akka.system.actorOf(Props[Login])

  def index = CSRFAddToken {
    RequiresUnautheticated { implicit request: Request[_] =>
      Ok(views.html.index(AuthenticateForm.form))
    }
  }

  def authenticate = CSRFCheck {
    RequiresUnautheticated.async { implicit request =>

      AuthenticateForm.
        form.
        bindFromRequest.
        fold(
          formWithErrors => {
            Future { BadRequest(views.html.index(AuthenticateForm.form)) }
          },
          authToken => {
            val login = Login.DoLogin(authToken)

            (loginSrv ? login).
              map { player =>
                login.
                  getResult(player).
                  map(Security.userToRequest(Results.Redirect(controllers.routes.Player.index), _)).
                  getOrElse(Redirect(routes.Application.index))
              }
          })
    }
  }
}