package components

import akka.util.Timeout
import scala.concurrent.duration._

trait DbTimeout {
  val timeoutDuration = 60 seconds
  implicit val timeout = Timeout(timeoutDuration)
}