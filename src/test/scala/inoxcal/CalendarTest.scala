package inoxcal

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import java.time.LocalDate

/**
 * Unit tests for the Calendar class. The calendar is pinned to English so that the
 * assertions do not depend on the locale of the machine running the tests.
 */
class CalendarTest extends AnyFunSuite with Matchers:

  val calendar = Calendar.withLanguage("en")

  test("getDaysInMonth should return correct number of days"):
    calendar.getDaysInMonth(2024, 1) shouldBe 31  // January
    calendar.getDaysInMonth(2024, 2) shouldBe 29  // February (leap year)
    calendar.getDaysInMonth(2023, 2) shouldBe 28  // February (non-leap year)
    calendar.getDaysInMonth(2024, 4) shouldBe 30  // April
    calendar.getDaysInMonth(2024, 12) shouldBe 31 // December

  test("isLeapYear should correctly identify leap years"):
    calendar.isLeapYear(2024) shouldBe true   // Divisible by 4
    calendar.isLeapYear(2023) shouldBe false  // Not divisible by 4
    calendar.isLeapYear(1900) shouldBe false  // Divisible by 100 but not 400
    calendar.isLeapYear(2000) shouldBe true   // Divisible by 400

  test("getDayOfWeek should return correct day of week"):
    // January 1, 2024 was a Monday (1)
    calendar.getDayOfWeek(2024, 1, 1) shouldBe 1
    // January 7, 2024 was a Sunday (0)
    calendar.getDayOfWeek(2024, 1, 7) shouldBe 0
    // December 25, 2024 is a Wednesday (3)
    calendar.getDayOfWeek(2024, 12, 25) shouldBe 3

  test("displayMonth should include month name and year in header"):
    val result = calendar.displayMonth(2024, 3)
    result should include("March 2024")

  test("displayMonth should include day names header"):
    val result = calendar.displayMonth(2024, 3)
    result should include("Su Mo Tu We Th Fr Sa")

  test("monthView should rotate weekdays and place dates under the rotated header"):
    val view = calendar.monthView(2024, 3, startingDay = 1)

    view.weekdays shouldBe Vector(1, 2, 3, 4, 5, 6, 0)
    view.weeks.head.days.head shouldBe None
    view.weeks.head.days(4).map(_.toString) shouldBe Some("2024-03-01")
    calendar.displayMonth(2024, 3, startingDay = 1, weekNumbers = false) should include("Mo Tu We Th Fr Sa Su")

  test("week-number rendering should include ISO week numbers"):
    val result = calendar.displayMonth(2024, 3, startingDay = 1, weekNumbers = true)

    result should include("Wk Mo Tu We Th Fr Sa Su")
    result should include(" 9")

  test("colored rendering should use TamboUI ANSI styles"):
    val colored = CalendarTextRenderer.renderMonthColored(calendar.monthView(2024, 3))

    colored should include("\u001b[")
    colored should include("March 2024")

  test("displayMonth should format March 2024 correctly"):
    val result = calendar.displayMonth(2024, 3)
    val lines = result.split("\n")

    // Should have header, day names, and calendar grid
    lines.length should be >= 3

    // First line should be the header
    lines(0) should include("March 2024")

    // Second line should be day names
    lines(1) shouldBe "Su Mo Tu We Th Fr Sa"

    // March 1, 2024 was a Friday, so it should start in the 6th column (index 5)
    val firstWeek = lines(2)
    firstWeek should include(" 1")

    // Should contain all days 1-31
    val fullCalendar = result
    for day <- 1 to 31 do
      fullCalendar should include(f"$day%2d")

  test("displayCurrentMonth should work without throwing exceptions"):
    val result = calendar.displayCurrentMonth()
    result should not be empty
    val currentYear = LocalDate.now().getYear
    result should include(currentYear.toString)

  test("displayYear should include year in header"):
    val result = calendar.displayYear(2024)
    result should include("2024")

  test("displayYear should include all 12 months"):
    val result = calendar.displayYear(2024)
    val monthNames = calendar.getLocalizationManager.getAllMonthNames

    for monthName <- monthNames do
      result should include(monthName)

  test("displayCurrentYear should work without throwing exceptions"):
    val result = calendar.displayCurrentYear()
    result should not be empty
    val currentYear = LocalDate.now().getYear
    result should include(currentYear.toString)

  test("calendar grid should be properly aligned"):
    val result = calendar.displayMonth(2024, 1)
    val lines = result.split("\n").drop(2) // Skip header and day names

    lines.length should be >= 4

    for line <- lines if line.nonEmpty do
      // Exactly 7 two-character day slots separated by single spaces
      line.length shouldBe 20

  test("edge case: February in leap year vs non-leap year"):
    val leap2024 = calendar.displayMonth(2024, 2)
    val nonLeap2023 = calendar.displayMonth(2023, 2)

    leap2024 should include("29")
    nonLeap2023 should not include "29"

  test("edge case: months with 30 vs 31 days"):
    val april = calendar.displayMonth(2024, 4) // 30 days
    val may = calendar.displayMonth(2024, 5)   // 31 days

    april should not include "31"
    may should include("31")
