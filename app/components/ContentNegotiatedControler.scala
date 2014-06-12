package components

import play.api.mvc.Request
import play.api.http.Writeable
import play.api.http.MediaRange
import play.api.libs.json._
import play.api.libs.json.util.LazyHelper
import play.libs.XML
import play.api.templates.Xml
import play.api.mvc.Results.Status
import play.api.mvc.SimpleResult
import play.api.mvc.Results
import play.api.mvc.Content
import play.api.mvc.Results.Status
import play.api.mvc.SimpleResult
import play.api.mvc.ActionBuilder
import play.api.mvc.Action
import scala.parallel.Future
import play.api.mvc.Controller

class ContentNegotiatedControler extends Controller {

  def render[model, viewContent](resp: Status, view: model => viewContent, mod: model)(implicit request: Request[_], writes: Writes[model], writer: Writeable[viewContent]): SimpleResult = {
    render {
      case Accepts.Html() => resp(view(mod))
      case Accepts.Json() => resp(Json.toJson(mod))
    }
  }

  def render[model1, model2, viewContent](resp: Status, view: (model1, model2) => viewContent, mod: (model1, model2))(implicit request: Request[_], writes: Writes[(model1, model2)], writer: Writeable[viewContent]): SimpleResult = {
    render {
      case Accepts.Html() => resp(view(mod._1, mod._2))
      case Accepts.Json() => resp(Json.toJson(mod))
    }
  }

  def render[model1, model2, model3, viewContent](resp: Status, view: (model1, model2, model3) => viewContent, mod: (model1, model2, model3))(implicit request: Request[_], writes: Writes[(model1, model2, model3)], writer: Writeable[viewContent]): SimpleResult = {
    render {
      case Accepts.Html() => resp(view(mod._1, mod._2, mod._3))
      case Accepts.Json() => resp(Json.toJson(mod))
    }
  }
}