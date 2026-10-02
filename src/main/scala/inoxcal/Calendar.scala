package inoxcal

import java.time._

/**
 * Calendar utility class that provides functionality similar to ncal
 */
class Calendar(private val l10n: LocalizationManager = new LocalizationManager()):
  
  /**
   * Display a calendar for the specified month and year
   */
  def displayMonth(year: Int, month: Int): String =
    CalendarTextRenderer.renderMonth(monthView(year, month))

  /**
   * Display a calendar with a rotated first weekday and optional week numbers.
   * `startingDay` uses Sunday = 0 through Saturday = 6.
   */
  def displayMonth(year: Int, month: Int, startingDay: Int, weekNumbers: Boolean): String =
    CalendarTextRenderer.renderMonth(monthView(year, month, startingDay, weekNumbers))
  
  /**
   * Display a calendar for the current month
   */
  def displayCurrentMonth(): String =
    val now = LocalDate.now()
    displayMonth(now.getYear, now.getMonthValue)
  
  /**
   * Display a full year calendar
   */
  def displayYear(year: Int): String =
    val header = s"                             $year\n"
    val months = (1 to 12).map(month => displayMonth(year, month)).mkString("\n\n")
    header + months

  /** Display a year with the requested calendar layout options. */
  def displayYear(year: Int, startingDay: Int, weekNumbers: Boolean): String =
    val header = s"                             $year\n"
    val months = (1 to 12)
      .map(month => displayMonth(year, month, startingDay, weekNumbers))
      .mkString("\n\n")
    header + months
  
  /**
   * Display the current year calendar
   */
  def displayCurrentYear(): String =
    val currentYear = LocalDate.now().getYear
    displayYear(currentYear)
  
  /** Build structured calendar data for a month. */
  def monthView(year: Int, month: Int, startingDay: Int = 0, weekNumbers: Boolean = false): MonthView =
    Calendar.validateStartingDay(startingDay)
    val firstDay = LocalDate.of(year, month, 1)
    val lastDay = firstDay.plusMonths(1).minusDays(1)
    val daysInMonth = lastDay.getDayOfMonth
    val firstDayOfWeek = firstDay.getDayOfWeek.getValue % 7
    val offset = (firstDayOfWeek - startingDay + 7) % 7
    val cells = Vector.tabulate(((offset + daysInMonth + 6) / 7) * 7) { index =>
      val day = index - offset + 1
      if day >= 1 && day <= daysInMonth then Some(LocalDate.of(year, month, day)) else None
    }
    val weeks = cells.grouped(7).toVector
    val weekdays = (0 until 7).map(day => (startingDay + day) % 7).toVector
    val rows = weeks.map { week =>
      val lastInMonthDate = week.reverse.collectFirst { case Some(date) => date }
      MonthWeek(week, lastInMonthDate.map(_.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)))
    }
    MonthView(year, month, l10n.getMonthName(month), weekdays, rows, weekNumbers, l10n)
  
  /**
   * Get number of days in a month
   */
  def getDaysInMonth(year: Int, month: Int): Int =
    LocalDate.of(year, month, 1).plusMonths(1).minusDays(1).getDayOfMonth
  
  /**
   * Check if a year is a leap year
   */
  def isLeapYear(year: Int): Boolean =
    Year.of(year).isLeap
  
  /**
   * Get the day of week for a specific date (0 = Sunday, 6 = Saturday)
   */
  def getDayOfWeek(year: Int, month: Int, day: Int): Int =
    LocalDate.of(year, month, day).getDayOfWeek.getValue % 7
  
  /**
   * Get the localization manager used by this calendar
   */
  def getLocalizationManager: LocalizationManager = l10n

object Calendar:
  /** Smallest year accepted by the command-line interface (inclusive). */
  val MinYear: Int = 1900

  /** Largest year accepted by the command-line interface (inclusive). */
  val MaxYear: Int = 3000

  /** First month of the year (inclusive). */
  val MinMonth: Int = 1

  /** Last month of the year (inclusive). */
  val MaxMonth: Int = 12

  /** First weekday value accepted by the CLI (Sunday). */
  val MinStartingDay: Int = 0

  /** Last weekday value accepted by the CLI (Saturday). */
  val MaxStartingDay: Int = 6

  def validateStartingDay(startingDay: Int): Unit =
    require(
      startingDay >= MinStartingDay && startingDay <= MaxStartingDay,
      s"Starting day must be between $MinStartingDay and $MaxStartingDay."
    )

  /**
   * Create a Calendar with a specific locale
   */
  def withLocale(locale: java.util.Locale): Calendar =
    new Calendar(new LocalizationManager(locale))
  
  /**
   * Create a Calendar with a specific language
   */
  def withLanguage(languageTag: String): Calendar =
    new Calendar(LocalizationManager.forLanguage(languageTag))

case class MonthWeek(days: Vector[Option[java.time.LocalDate]], weekNumber: Option[Int])

case class MonthView(
    year: Int,
    month: Int,
    monthName: String,
    weekdays: Vector[Int],
    weeks: Vector[MonthWeek],
    weekNumbers: Boolean,
    l10n: LocalizationManager
)