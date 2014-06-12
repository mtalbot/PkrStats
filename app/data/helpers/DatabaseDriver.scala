package data.helpers

object DatabaseDriver {
  val slickDriver = scala.slick.driver.H2Driver.simple
  val jodaDriver = com.github.tototoshi.slick.H2JodaSupport
}