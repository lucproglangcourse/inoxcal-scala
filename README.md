# Scalendar - Scala Calendar Utility

A command-line calendar utility similar to `ncal`, written in Scala.

## Features

- Display current month calendar
- Display specific month and year
- Display entire year calendar
- Leap year support
- **Internationalization (i18n) support** - Month names, day names, and error messages in multiple languages
- Clean, formatted output similar to traditional Unix cal/ncal utilities
- **Configurable localization** via command-line options

## Usage

### Basic Usage

```bash
# Display current month
sbt run

# Display help
sbt "runMain scalendar.ScalendarApp --help"

# Display specific month in current year (e.g., March)
sbt "runMain scalendar.ScalendarApp --month 3"

# Display specific month and year (e.g., March 2024)
sbt "runMain scalendar.ScalendarApp --month 3 --year 2024"

# Display entire current year
sbt "runMain scalendar.ScalendarApp --year-view"

# Display specific year
sbt "runMain scalendar.ScalendarApp --year-view --year 2024"

# Display calendar in Spanish
sbt "runMain scalendar.ScalendarApp --month 3 --year 2024 --locale es"

# Display calendar in French  
sbt "runMain scalendar.ScalendarApp --month 3 --year 2024 --locale fr"
```

### Command Line Options

- `--help` or `-h`: Show usage information
- `--year-view` or `-y`: Display entire year calendar
- `--month <int>`: Specify month (1-12) to display
- `--year <int>`: Specify year to display
- `--locale <string>` or `-l`: Set locale for internationalization (e.g., 'es' for Spanish, 'fr' for French)

Notes:

- `--year` on its own prints the year view, `--month` on its own uses the current year.
- `--year-view` takes precedence over `--month` when both are given.
- Accepted range: months `1`-`12` and years `1900`-`3000`. Invalid values are
  reported on stderr and the process exits with status `1`.
- The `Calendar` class itself is not limited to that range; the range is enforced
  by the command-line interface (see `Calendar.MinYear`/`MaxYear`).

### Examples

Display March 2024 in English (default):
```
    March 2024
Su Mo Tu We Th Fr Sa
                1  2
 3  4  5  6  7  8  9
10 11 12 13 14 15 16
17 18 19 20 21 22 23
24 25 26 27 28 29 30
31
```

Display March 2024 in Spanish:
```
    Marzo 2024
Do Lu Ma Mi Ju Vi Sa
                1  2
 3  4  5  6  7  8  9
10 11 12 13 14 15 16
17 18 19 20 21 22 23
24 25 26 27 28 29 30
31
```

Display March 2024 in French:
```
    Mars 2024
Di Lu Ma Me Je Ve Sa
                1  2
 3  4  5  6  7  8  9
10 11 12 13 14 15 16
17 18 19 20 21 22 23
24 25 26 27 28 29 30
31
```

## Internationalization Support

Scalendar supports multiple languages through Java's ResourceBundle system. The following languages are currently supported:

- **English (en)** - Default
- **Spanish (es)** - Español
- **French (fr)** - Français

### Supported Locales

Use the `--locale` or `-l` option to specify a language:

```bash
# English (default)
sbt "run --month 3 --year 2024"

# Spanish
sbt "run --month 3 --year 2024 --locale es"

# French
sbt "run --month 3 --year 2024 --locale fr"
```

### Adding New Languages

To add support for a new language:

1. Create a new properties file in `src/main/resources/scalendar/` named `messages_XX.properties` where `XX` is the language code
2. Translate all the keys from `messages.properties`
3. The new locale is picked up automatically by the `--locale` option for month
   names, day names, error messages, and the help description/examples (`de`
   falls back to the built-in English bundle because no `messages_de.properties`
   exists yet)

Two escaping rules matter when translating:

- `error.invalid.month` and `error.invalid.year` are rendered with `java.text.MessageFormat`,
  so a literal apostrophe must be doubled (`L''année`)
- the month, day, and help strings are used verbatim, so a single apostrophe is correct there (`toute l'année`)

Example for German (`messages_de.properties`):
```properties
month.1=Januar
month.2=Februar
month.3=März
# ... etc
day.0=So
day.1=Mo
# ... etc
error.invalid.month=Ungültiger Monat: {0}. Der Monat muss zwischen {1} und {2} liegen.
```

## Building and Running

### Prerequisites

- Scala 3.8.4 (configured in `scala.sbt`, using modern significant indentation syntax)
- sbt 1.13.0 (pinned in `project/build.properties`) running on JDK 25 (as in CI)

### Build

```bash
sbt compile
```

### Run

```bash
sbt run
```

### Run Tests

```bash
sbt test
```

### Create Executable

```bash
sbt stage
```

This creates an executable script in `target/universal/stage/bin/scalendar-scala`.

## Project Structure

```
src/
├── main/
│   ├── scala/scalendar/
│   │   ├── Calendar.scala              # Core calendar logic
│   │   ├── ScalendarApp.scala         # Command-line interface
│   │   └── LocalizationManager.scala  # Internationalization support
│   └── resources/scalendar/
│       ├── messages.properties         # English (default)
│       ├── messages_es.properties      # Spanish
│       └── messages_fr.properties      # French
└── test/scala/scalendar/
    ├── CalendarTest.scala            # Unit tests for the Calendar class
    ├── LocalizationManagerTest.scala # Tests for the resource bundles and i18n API
    ├── ScalendarAppTest.scala        # Tests for the command-line interface
    └── IntegrationTest.scala         # Integration tests and exact output snapshots
```

Build files: `build.sbt`, `scala.sbt` (compiler options), `project/plugins.sbt`
and `project/build.properties` (pinned sbt version). CI runs
`coverage test coverageReport` on JDK 25; `doc/ai/` holds the AI transcripts and
the remediation plan (`doc/ai/20260930_RemediationPlan.md`).

## API Reference

### Calendar Class

- `displayMonth(year, month)`: Display a specific month
- `displayCurrentMonth()`: Display current month  
- `displayYear(year)`: Display entire year
- `displayCurrentYear()`: Display current year
- `getDaysInMonth(year, month)`: Get number of days in a month
- `isLeapYear(year)`: Check if a year is a leap year
- `getDayOfWeek(year, month, day)`: Get day of week for a date

The companion object exposes the range accepted by the command-line interface:
`Calendar.MinYear`/`Calendar.MaxYear` (`1900`-`3000`) and
`Calendar.MinMonth`/`Calendar.MaxMonth` (`1`-`12`).

### Calendar Factory Methods

- `Calendar.withLocale(locale)`: Create calendar with specific Java Locale
- `Calendar.withLanguage(languageTag)`: Create calendar with specific language

### LocalizationManager Class

- `getMonthName(month)`: Get localized month name
- `getDayName(dayOfWeek)`: Get localized day name  
- `getInvalidMonthError(month)`: Get localized error message for invalid month
- `getInvalidYearError(year)`: Get localized error message for invalid year
- `getAllMonthNames` / `getAllDayNames`: Get all names as an immutable `IndexedSeq`
- `getHelpDescription` / `getExamplesText`: Get the localized help strings
- `getCurrentLocale`: Get the locale in use
- `LocalizationManager.forLanguage(tag)`: Create manager for specific language
- `LocalizationManager.forLocale(language, country)`: Create manager for a specific locale

### Command Line Interface

- Argument parsing and validation with [mainargs](https://github.com/com-lihaoyi/mainargs)
- `validateMonth`/`validateYear` return `Either[String, Unit]`, so the failure paths are unit testable
- Invalid values are reported on stderr with exit status `1`
- Localized help system (`--help` combines the translated description and examples with the generated option list)

## Testing

The project includes comprehensive tests:

- **Unit Tests** (`CalendarTest`): individual methods and edge cases; every grid row must be exactly 20 characters wide
- **Localization Tests** (`LocalizationManagerTest`): bundle contents, locale fallback, and error messages for en/es/fr
- **Application Tests** (`ScalendarAppTest`): option parsing, validation failures through `Either`, and end-to-end output captured with `Console.withOut`
- **Integration Tests** (`IntegrationTest`): complete workflows plus exact ("golden") output snapshots

Tests run in a forked JVM with the locale pinned to `en_US` (see `build.sbt`), so
the output assertions do not depend on the machine running them.

Run all tests with:
```bash
sbt test
```

Coverage is produced with a minimum statement-coverage gate (see `build.sbt`):

```bash
sbt coverage test coverageReport
```

## Development

### Adding New Features

1. Add functionality to the `Calendar` class
2. Update the `ScalendarApp` object for command-line interface changes
3. Add corresponding tests
4. Update this README

### Code Style

The project follows modern Scala 3 conventions:
- Uses significant indentation (no curly braces)
- Use camelCase for methods and variables
- Use PascalCase for classes and objects
- Include comprehensive documentation
- Write tests for all new functionality


## License

MIT - see [LICENSE](LICENSE), © 2025 LUC COMP 371/471 Prog Language Course.

## AI Disclosure

This text contains a mix of original writing and programming with strategic use of ChatGPT via intentional prompting.
We may also make some prompts and analyses available, similar to what my colleagues have done for their recent ongoing study of ChatGPT and Systems Programming.
See also https://doi.org/10.6084/m9.figshare.22257274.
