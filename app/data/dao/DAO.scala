package data.dao

import akka.actor.ActorRef
import akka.pattern.{ ask, pipe }
import components.DbTimeout
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Helper object for sending typed operations to actors
 */
object DAO extends DbTimeout {
  /**
   * send the operation to the actor and transform the result to the correct type
   */
  def apply[ReturnType](dao: ActorRef, op: Operation[ReturnType]): Future[ReturnType] = (dao ? op).map(op.getResult(_))
}