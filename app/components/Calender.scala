package components

import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Months

object Calender {
  def apply(start: DateTime, end: DateTime) = {
    val totalMonths = Months.monthsBetween(start.withTimeAtStartOfDay(), end.withTimeAtStartOfDay()).getMonths

    val startingMonth = start.withDayOfMonth(1)

    val months = (for {
      month <- Range(0, totalMonths + 1)
    } yield startingMonth.plusMonths(month)).
      groupBy(_.getYear).
      toList.
      sortBy(_._1).
      map { value =>
        (value._1, value._2.map { month =>
          val monthOfYear = month.monthOfYear
          val lastDay = month.dayOfMonth.withMaximumValue
          val lastWeek = lastDay.getWeekOfWeekyear match {
            case week if week < 52 && monthOfYear.get == 12 => 52
            case week => week
          } 
          val firstWeek = month.getWeekOfWeekyear match {
            case week if week >= 52 && monthOfYear.get == 1 => 0
            case week => week
          }
          val weeks = lastWeek - firstWeek
          (monthOfYear.get, (monthOfYear.getAsText, month.dayOfWeek.get, lastDay.dayOfMonth.get, weeks))
        }.sortBy(_._1))
      }

    val secondWeek = new DateTime().withWeekOfWeekyear(2)

    val days = (for {
      day <- Range(1, 8)
    } yield secondWeek.withDayOfWeek(day).dayOfWeek).map { day =>
      (day.get, day.getAsText)
    }.sortBy(_._1)

    (days, months)
  }
}