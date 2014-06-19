package components

import play.api.mvc.QueryStringBindable
import play.api.mvc.PathBindable

object LongBinder {
  implicit def binder(implicit longBinder: PathBindable[Long]) = new PathBindable[Option[Long]] {
    override def bind(key: String, value: String): Either[String, Option[Long]] = {
      longBinder.bind(key, value).fold(Left(_), { valu => Right(Some(valu))} )
    }
    override def unbind(key: String, value: Option[Long]): String = value.map(_.toString).getOrElse("")
  }
}