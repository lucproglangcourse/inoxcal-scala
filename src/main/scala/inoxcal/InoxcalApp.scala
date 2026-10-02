package inoxcal

import mainargs.{main, arg, ParserForMethods, Flag}
import java.time.LocalDate

/**
 * Main application for the Inoxcal command-line calendar utility
 */
object InoxcalApp:

  @main
  def inoxcal(
    @arg(name = "month", doc = "Month (1-12) to display")
    month: Option[Int] = None,

    @arg(name = "year", doc = "Year to display")
    year: Option[Int] = None,

    @arg(short = 'y', name = "year-view", doc = "Display the entire year")
    yearView: Flag = Flag(),

    @arg(short = 'l', name = "locale", doc = "Set locale (e.g., 'es' for Spanish, 'fr' for French)")
    locale: Option[String] = None,

    @arg(short = 'h', name = "help", doc = "Show this help message")
    help: Flag = Flag()
  ): Unit =
    // Build a single calendar, localized when a locale was requested
    val calendar = locale.fold(new Calendar)(Calendar.withLanguage)
    val l10n = calendar.getLocalizationManager

    val result: Either[String, String] =
      if help.value then
        Right(localizedHelp(l10n))
      else if yearView.value then
        val targetYear = year.getOrElse(LocalDate.now().getYear)
        validateYear(targetYear, l10n).map(_ => calendar.displayYear(targetYear))
      else
        (month, year) match
          case (Some(m), Some(y)) =>
            for
              _ <- validateMonth(m, l10n)
              _ <- validateYear(y, l10n)
            yield calendar.displayMonth(y, m)

          case (Some(m), None) =>
            val currentYear = LocalDate.now().getYear
            validateMonth(m, l10n).map(_ => calendar.displayMonth(currentYear, m))

          case (None, Some(y)) =>
            validateYear(y, l10n).map(_ => calendar.displayYear(y))

          case (None, None) =>
            // No arguments - show the current month
            Right(calendar.displayCurrentMonth())

    result match
      case Right(text) => println(text)
      case Left(error) =>
        System.err.println(error)
        sys.exit(1)

  /**
   * Localized help text: a translated description and examples wrapped around the
   * option list rendered by mainargs.
   */
  def localizedHelp(l10n: LocalizationManager): String =
    s"${l10n.getHelpDescription}\n\n${ParserForMethods(this).helpText()}\n${l10n.getExamplesText}"

  /**
   * Check that a month is within the range accepted by the command-line
   * interface, see [[Calendar]].
   */
  def validateMonth(month: Int, l10n: LocalizationManager): Either[String, Unit] =
    if month < Calendar.MinMonth || month > Calendar.MaxMonth then
      Left(l10n.getInvalidMonthError(month))
    else
      Right(())

  /**
   * Check that a year is within the range accepted by the command-line
   * interface, see [[Calendar]].
   */
  def validateYear(year: Int, l10n: LocalizationManager): Either[String, Unit] =
    if year < Calendar.MinYear || year > Calendar.MaxYear then
      Left(l10n.getInvalidYearError(year))
    else
      Right(())

  def main(args: Array[String]): Unit =
    val arguments = args.toIndexedSeq

    // mainargs prints its own English help text and exits when "--help" is the first
    // argument, so the localized help is printed here instead. Other spellings of the
    // flag ("-h", or "--help" later in the argument list) are handled by the parser.
    if arguments.headOption.contains("--help") then
      println(localizedHelp(localizationManagerFor(arguments)))
    else
      ParserForMethods(this).runOrExit(arguments): Unit

  /**
   * Localization manager for the locale requested on the command line
   * (`-l es`, `--locale es`, or `--locale=es`), or for the default locale.
   */
  private def localizationManagerFor(args: IndexedSeq[String]): LocalizationManager =
    val languageTag =
      args.indexWhere(argument => argument == "-l" || argument == "--locale") match
        case -1 => args.find(_.startsWith("--locale=")).map(_.split("=", 2)(1))
        case index => args.lift(index + 1)

    languageTag match
      case Some(tag) => LocalizationManager.forLanguage(tag)
      case None => new LocalizationManager()
