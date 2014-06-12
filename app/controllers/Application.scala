package controllers

import play.api._
import play.api.mvc._
import components.ContentNegotiatedControler
import components.DbTimeout

object Application extends ContentNegotiatedControler with DbTimeout {

  def index = Action { implicit request: Request[_] =>
    Ok(views.html.index("Your new application is ready."))
  }

}