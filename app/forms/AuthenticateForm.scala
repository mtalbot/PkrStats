package forms

import org.joda.time.DateTime
import play.api.data._
import play.api.data.Forms._

object AuthenticateForm {
  case class AuthToken(id: String, code: String, authtype: String)

  val form = Form(
    mapping(
      "id" -> text,
      "code" -> text,
      "authtype" -> text)(AuthToken.apply)(AuthToken.unapply))
}