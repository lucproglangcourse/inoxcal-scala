package inoxcal

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
class InoxcalAppTest extends AnyFunSuite with Matchers:

  private val l10n = new LocalizationManager(java.util.Locale.ENGLISH)

  /** Calendar using the same locale resolution as `InoxcalApp` by default. */
  private val defaultCalendar = new Calendar

  /** Capture what `body` prints to stdout, normalized to "\n" line endings. */
  private def captureOut(body: => Unit): String =
    val buffer = new java.io.ByteArrayOutputStream()
    Console.withOut(new java.io.PrintStream(buffer, true, "UTF-8")):
      body
    buffer.toString("UTF-8").replace("\r\n", "\n")

  test("mainargs parser should describe every supported option"):
    val helpText = ParserForMethods(InoxcalApp).helpText()

    helpText should include("inoxcal")
    helpText should include("month")
    helpText should include("year-view")
    helpText should include("locale")
    helpText should include("starting-day")
    helpText should include("week-numbers")
    helpText should include("color")
    helpText should include("help")

  test("localized help should add the translated description and examples"):
    val englishHelp = InoxcalApp.localizedHelp(l10n)
    englishHelp should include(l10n.getHelpDescription)
    englishHelp should include(l10n.getExamplesText)
    englishHelp should include("year-view")

    val spanishHelp = InoxcalApp.localizedHelp(LocalizationManager.forLanguage("es"))
    spanishHelp should include("utilidad de calendario")
    spanishHelp should include("--year-view")

  test("month validation should accept 1-12 and reject everything else"):
    for month <- Calendar.MinMonth to Calendar.MaxMonth do
      InoxcalApp.validateMonth(month, l10n).isRight shouldBe true

    InoxcalApp.validateMonth(0, l10n).isLeft shouldBe true
    InoxcalApp.validateMonth(13, l10n) match
      case Left(message) =>
        message should include("13")
        message should include("between 1 and 12")
      case Right(_) => fail("month 13 should have been rejected")

    l10n.getInvalidMonthError(13) should include("13")
    l10n.getInvalidMonthError(13) should include("between 1 and 12")

  test("year validation should accept the documented range and reject everything else"):
    for year <- Seq(Calendar.MinYear, 2024, Calendar.MaxYear) do
      InoxcalApp.validateYear(year, l10n).isRight shouldBe true

    InoxcalApp.validateYear(1899, l10n).isLeft shouldBe true
    InoxcalApp.validateYear(3001, l10n) match
      case Left(message) =>
        message should include("3001")
        message should include("between 1900 and 3000")
      case Right(_) => fail("year 3001 should have been rejected")

    l10n.getInvalidYearError(3001) should include("3001")
    l10n.getInvalidYearError(3001) should include("between 1900 and 3000")

  test("starting-day validation should accept Sunday through Saturday"):
    for startingDay <- Calendar.MinStartingDay to Calendar.MaxStartingDay do
      InoxcalApp.validateStartingDay(startingDay, l10n).isRight shouldBe true

    InoxcalApp.validateStartingDay(-1, l10n) match
      case Left(message) =>
        message should include("-1")
        message should include("between 0 and 6")
      case Right(_) => fail("starting day -1 should have been rejected")

    InoxcalApp.validateStartingDay(7, l10n).isLeft shouldBe true

  test("Calendar methods should be accessible"):
    val march2024 = defaultCalendar.displayMonth(2024, 3)
    march2024 should include("March 2024")

    val year2024 = defaultCalendar.displayYear(2024)
    year2024 should include("2024")
    year2024 should include("January")
    year2024 should include("December")

  test("running without arguments should print the current month"):
    val output = captureOut(InoxcalApp.inoxcal())

    output shouldBe s"${defaultCalendar.displayCurrentMonth()}\n"

  test("running with a month and a year should print that month"):
    val output = captureOut(InoxcalApp.inoxcal(month = Some(3), year = Some(2024)))

    output shouldBe s"${defaultCalendar.displayMonth(2024, 3)}\n"
    output should include("March 2024")

  test("running with only a month should use the current year"):
    val currentYear = LocalDate.now().getYear
    val output = captureOut(InoxcalApp.inoxcal(month = Some(3)))

    output shouldBe s"${defaultCalendar.displayMonth(currentYear, 3)}\n"

  test("running with only a year should print the whole year"):
    val output = captureOut(InoxcalApp.inoxcal(year = Some(2024)))

    output shouldBe s"${defaultCalendar.displayYear(2024)}\n"

  test("--year-view should take precedence over --month"):
    val output = captureOut(
      InoxcalApp.inoxcal(month = Some(3), year = Some(2024), yearView = Flag(true))
    )

    output shouldBe s"${defaultCalendar.displayYear(2024)}\n"
    output should include("December")

  test("positional year should print the whole year"):
    val output = captureOut(InoxcalApp.inoxcal(positionalYear = Some(2024)))

    output shouldBe s"${defaultCalendar.displayYear(2024)}\n"

  test("positional and named years should be rejected together"):
    InoxcalApp.validateYearSources(Some(2024), Some(2025), l10n) match
      case Left(message) => message should include("either positionally")
      case Right(_) => fail("both year forms should have been rejected")

  test("--locale should translate the output"):
    val output = captureOut(
      InoxcalApp.inoxcal(month = Some(3), year = Some(2024), locale = Some("es"))
    )

    output shouldBe s"${Calendar.withLanguage("es").displayMonth(2024, 3)}\n"
    output should include("Marzo 2024")
    output should include("Do Lu Ma Mi Ju Vi Sa")

  test("starting day, week numbers, and color flags should be accepted"):
    val output = captureOut(
      InoxcalApp.inoxcal(
        month = Some(3),
        year = Some(2024),
        startingDay = 1,
        weekNumbers = Flag(true),
        color = Flag(true)
      )
    )

    output should include("Wk Mo Tu We Th Fr Sa Su")
    output should include(" 9")

  test("--help should print the localized help with the option list"):
    val output = captureOut(InoxcalApp.inoxcal(help = Flag(true)))

    output should include(l10n.getHelpDescription)
    output should include(l10n.getExamplesText)
    output should include("year-view")

  test("a leading --help should print the localized help instead of mainargs help"):
    val output = captureOut(InoxcalApp.main(Array("--help")))

    output should include(l10n.getHelpDescription)
    output should include(l10n.getExamplesText)
    output should include("year-view")

  test("--help should honor the requested locale"):
    val output = captureOut(InoxcalApp.main(Array("--help", "--locale", "es")))

    output should include("utilidad de calendario")
    output should include("--year-view")

    val longForm = captureOut(InoxcalApp.main(Array("--help", "--locale=fr")))
    longForm should include("utilitaire de calendrier")

  test("-h should print the localized help as well"):
    val output = captureOut(InoxcalApp.main(Array("-h")))

    output should include(l10n.getHelpDescription)
    output should include("year-view")
