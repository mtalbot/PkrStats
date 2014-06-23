package components

import akka.util.Timeout
import scala.concurrent.duration._

trait DbTimeout {
  val timeoutDuration = 5 seconds
  implicit val timeout = Timeout(timeoutDuration)
}