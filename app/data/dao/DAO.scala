package data.dao

import akka.actor.ActorRef
import akka.pattern.{ ask, pipe }
import components.DbTimeout
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object DAO extends DbTimeout {
	def apply[ReturnType](dao: ActorRef, op: Operation[ReturnType]): Future[ReturnType] = (dao ? op).map(op.getResult(_))
}