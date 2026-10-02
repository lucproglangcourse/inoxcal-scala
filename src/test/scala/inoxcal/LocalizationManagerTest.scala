package inoxcal

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import java.util.Locale

class LocalizationManagerTest extends AnyFunSuite with Matchers:

  private val english = LocalizationManager.forLanguage("en")
  private val spanish = LocalizationManager.forLanguage("es")
  private val french = LocalizationManager.forLanguage("fr")

  test("month names should be localized"):
    english.getMonthName(3) shouldBe "March"
    spanish.getMonthName(3) shouldBe "Marzo"
    french.getMonthName(3) shouldBe "Mars"

  test("getAllMonthNames should return the twelve names in calendar order"):
    val names = english.getAllMonthNames
    names should have size 12
    names.head shouldBe "January"
    names(5) shouldBe "June"
    names.last shouldBe "December"

  test("getAllDayNames should return the seven names starting on Sunday"):
    english.getAllDayNames shouldBe IndexedSeq("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
    spanish.getAllDayNames.head shouldBe "Do"
    french.getAllDayNames.head shouldBe "Di"

  test("unsupported locales should fall back to English"):
    val german = LocalizationManager.forLanguage("de")
    german.getMonthName(3) shouldBe "March"
    german.getDayName(3) shouldBe "We"

  test("forLocale should build locales from a language and an optional region"):
    LocalizationManager.forLocale("es", "ES").getMonthName(3) shouldBe "Marzo"
    LocalizationManager.forLocale("fr").getMonthName(3) shouldBe "Mars"
    LocalizationManager.forLocale("de").getMonthName(3) shouldBe "March" // falls back to English

  test("the default manager should use the JVM default locale"):
    val defaultLocale = new LocalizationManager(Locale.getDefault)

    new LocalizationManager().getCurrentLocale.toString shouldBe
      defaultLocale.getCurrentLocale.toString

  test("invalid month errors should be localized and mention the value and the bounds"):
    english.getInvalidMonthError(13) shouldBe
      "Invalid month: 13. Month must be between 1 and 12."
    spanish.getInvalidMonthError(13) should include("Mes inválido")
    spanish.getInvalidMonthError(13) should include("13")
    french.getInvalidMonthError(13) should include("Mois invalide")

  test("invalid year errors should be localized and mention the value and the bounds"):
    english.getInvalidYearError(3001) shouldBe
      "Invalid year: 3001. Year must be between 1900 and 3000."
    spanish.getInvalidYearError(3001) should include("Año inválido")
    french.getInvalidYearError(3001) should include("Année invalide")

  test("every supported locale should resolve every key used by the application"):
    for manager <- Seq(english, spanish, french) do
      for month <- Calendar.MinMonth to Calendar.MaxMonth do
        manager.getMonthName(month) should not be empty
      for dayOfWeek <- 0 to 6 do
        manager.getDayName(dayOfWeek) should not be empty
      manager.getHelpDescription should not be empty
      manager.getExamplesText should not be empty
      manager.getInvalidMonthError(0) should not be empty
      manager.getInvalidYearError(0) should not be empty
