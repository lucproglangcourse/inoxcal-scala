package scalendar

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import java.time.LocalDate
import mainargs.{ParserForMethods, Flag}

/**
 * Tests for the command-line interface: option parsing, argument validation, and the
 * text the application prints. Assertions that involve localized output are compared
 * against a calendar built the same way as the application builds it, so the suite
 * does not depend on the locale of the machine running the tests.
 */
class ScalendarAppTest extends AnyFunSuite with Matchers:

  private val l10n = new LocalizationManager(java.util.Locale.ENGLISH)

  /** Calendar using the same locale resolution as `ScalendarApp` by default. */
  private val defaultCalendar = new Calendar

  /** Capture what `body` prints to stdout, normalized to "\n" line endings. */
  private def captureOut(body: => Unit): String =
    val buffer = new java.io.ByteArrayOutputStream()
    Console.withOut(new java.io.PrintStream(buffer, true, "UTF-8")):
      body
    buffer.toString("UTF-8").replace("\r\n", "\n")

  test("mainargs parser should describe every supported option"):
    val helpText = ParserForMethods(ScalendarApp).helpText()

    helpText should include("scalendar")
    helpText should include("month")
    helpText should include("year-view")
    helpText should include("locale")
    helpText should include("help")

  test("localized help should add the translated description and examples"):
    val englishHelp = ScalendarApp.localizedHelp(l10n)
    englishHelp should include(l10n.getHelpDescription)
    englishHelp should include(l10n.getExamplesText)
    englishHelp should include("year-view")

    val spanishHelp = ScalendarApp.localizedHelp(LocalizationManager.forLanguage("es"))
    spanishHelp should include("utilidad de calendario")
    spanishHelp should include("--year-view")

  test("month validation should accept 1-12 and reject everything else"):
    for month <- Calendar.MinMonth to Calendar.MaxMonth do
      ScalendarApp.validateMonth(month, l10n).isRight shouldBe true

    ScalendarApp.validateMonth(0, l10n).isLeft shouldBe true
    ScalendarApp.validateMonth(13, l10n) match
      case Left(message) =>
        message should include("13")
        message should include("between 1 and 12")
      case Right(_) => fail("month 13 should have been rejected")

    l10n.getInvalidMonthError(13) should include("13")
    l10n.getInvalidMonthError(13) should include("between 1 and 12")

  test("year validation should accept the documented range and reject everything else"):
    for year <- Seq(Calendar.MinYear, 2024, Calendar.MaxYear) do
      ScalendarApp.validateYear(year, l10n).isRight shouldBe true

    ScalendarApp.validateYear(1899, l10n).isLeft shouldBe true
    ScalendarApp.validateYear(3001, l10n) match
      case Left(message) =>
        message should include("3001")
        message should include("between 1900 and 3000")
      case Right(_) => fail("year 3001 should have been rejected")

    l10n.getInvalidYearError(3001) should include("3001")
    l10n.getInvalidYearError(3001) should include("between 1900 and 3000")

  test("Calendar methods should be accessible"):
    val march2024 = defaultCalendar.displayMonth(2024, 3)
    march2024 should include("March 2024")

    val year2024 = defaultCalendar.displayYear(2024)
    year2024 should include("2024")
    year2024 should include("January")
    year2024 should include("December")

  test("running without arguments should print the current month"):
    val output = captureOut(ScalendarApp.scalendar())

    output shouldBe s"${defaultCalendar.displayCurrentMonth()}\n"

  test("running with a month and a year should print that month"):
    val output = captureOut(ScalendarApp.scalendar(month = Some(3), year = Some(2024)))

    output shouldBe s"${defaultCalendar.displayMonth(2024, 3)}\n"
    output should include("March 2024")

  test("running with only a month should use the current year"):
    val currentYear = LocalDate.now().getYear
    val output = captureOut(ScalendarApp.scalendar(month = Some(3)))

    output shouldBe s"${defaultCalendar.displayMonth(currentYear, 3)}\n"

  test("running with only a year should print the whole year"):
    val output = captureOut(ScalendarApp.scalendar(year = Some(2024)))

    output shouldBe s"${defaultCalendar.displayYear(2024)}\n"

  test("--year-view should take precedence over --month"):
    val output = captureOut(
      ScalendarApp.scalendar(month = Some(3), year = Some(2024), yearView = Flag(true))
    )

    output shouldBe s"${defaultCalendar.displayYear(2024)}\n"
    output should include("December")

  test("--locale should translate the output"):
    val output = captureOut(
      ScalendarApp.scalendar(month = Some(3), year = Some(2024), locale = Some("es"))
    )

    output shouldBe s"${Calendar.withLanguage("es").displayMonth(2024, 3)}\n"
    output should include("Marzo 2024")
    output should include("Do Lu Ma Mi Ju Vi Sa")

  test("--help should print the localized help with the option list"):
    val output = captureOut(ScalendarApp.scalendar(help = Flag(true)))

    output should include(l10n.getHelpDescription)
    output should include(l10n.getExamplesText)
    output should include("year-view")

  test("a leading --help should print the localized help instead of mainargs help"):
    val output = captureOut(ScalendarApp.main(Array("--help")))

    output should include(l10n.getHelpDescription)
    output should include(l10n.getExamplesText)
    output should include("year-view")

  test("--help should honor the requested locale"):
    val output = captureOut(ScalendarApp.main(Array("--help", "--locale", "es")))

    output should include("utilidad de calendario")
    output should include("--year-view")

    val longForm = captureOut(ScalendarApp.main(Array("--help", "--locale=fr")))
    longForm should include("utilitaire de calendrier")

  test("-h should print the localized help as well"):
    val output = captureOut(ScalendarApp.main(Array("-h")))

    output should include(l10n.getHelpDescription)
    output should include("year-view")
