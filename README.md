# Inoxcal - Rust-free Calendar Utility Written in Scala

A command-line calendar utility similar to `ncal`, written in Scala. The name nods
to *inox*, Castilian for "stainless steel" - see [Warranty](#warranty).

## Features

- Display current month calendar
- Display specific month and year
- Display entire year calendar
- Leap year support
- Optional positional year argument
- Configurable starting weekday (`--starting-day 0..6`)
- Optional ISO week numbers (`-w`, `--week-numbers`)
- Optional TamboUI-colored output (`-c`, `--color`)
- **Internationalization (i18n) support** - Month names, day names, and error messages in multiple languages
- Clean, formatted output similar to traditional Unix cal/ncal utilities
- **Configurable localization** via command-line options

## Usage

### Basic Usage

```bash
# Display current month
sbt run

# Display help
sbt "runMain inoxcal.InoxcalApp --help"

# Display specific month in current year (e.g., March)
sbt "runMain inoxcal.InoxcalApp --month 3"

# Display specific month and year (e.g., March 2024)
sbt "runMain inoxcal.InoxcalApp --month 3 --year 2024"

# Display entire current year
sbt "runMain inoxcal.InoxcalApp --year-view"

# Display specific year
sbt "runMain inoxcal.InoxcalApp --year-view --year 2024"

# Display a year using the positional argument
sbt "runMain inoxcal.InoxcalApp 2024"

# Start weeks on Monday and show ISO week numbers
sbt "runMain inoxcal.InoxcalApp --month 3 --year 2024 --starting-day 1 --week-numbers"

# Enable TamboUI-colored output when running in a real terminal
sbt "runMain inoxcal.InoxcalApp --month 3 --year 2024 --color"

# Display calendar in Spanish
sbt "runMain inoxcal.InoxcalApp --month 3 --year 2024 --locale es"

# Display calendar in French  
sbt "runMain inoxcal.InoxcalApp --month 3 --year 2024 --locale fr"
```

### Command Line Options

- `--help` or `-h`: Show usage information
- `--year-view` or `-y`: Display entire year calendar
- `[<year>]`: Optional positional year; equivalent to `--year <year>`
- `--month <int>`: Specify month (1-12) to display
- `--year <int>`: Specify year to display
- `--locale <string>` or `-l`: Set locale for internationalization (e.g., 'es' for Spanish, 'fr' for French)
- `--starting-day <int>`: Set the first weekday, where Sunday is `0` and Saturday is `6`
- `--week-numbers` or `-w`: Display ISO week numbers for each calendar row
- `--color` or `-c`: Enable TamboUI-styled colors when stdout is a real terminal

Notes:

- `--year` on its own prints the year view, `--month` on its own uses the current year.
- A positional year prints the year view. Supplying both a positional year and
  `--year` is rejected.
- `--year-view` takes precedence over `--month` when both are given.
- Accepted range: months `1`-`12` and years `1900`-`3000`. Invalid values are
  reported on stderr and the process exits with status `1`.
- Starting days must be in the range `0`-`6`; day names and date columns rotate
  together. Week numbers use ISO week numbering.
- `--color` uses TamboUI styles for headings, weekends, today, and week numbers.
  Color is automatically omitted when stdout is not attached to a real terminal.
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

Inoxcal supports multiple languages through Java's ResourceBundle system. The following languages are currently supported:

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

1. Create a new properties file in `src/main/resources/inoxcal/` named `messages_XX.properties` where `XX` is the language code
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
- JDK 17 or newer at runtime. JDK 17, 21, and 25 are tested in release CI.
- sbt 1.13.0 (pinned in `project/build.properties`) for building from source

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

This creates an executable script in `target/universal/stage/bin/inoxcal`.

### Install a Distribution Archive

Release archives are named `inoxcal-<version>.zip` and `inoxcal-<version>.tar.gz`.
Download either archive from the project's GitHub Release, extract it, and run:

```bash
./inoxcal-<version>/bin/inoxcal --help
./inoxcal-<version>/bin/inoxcal 2024
```

The archive includes this README, the MIT license, the `inoxcal` launcher, and all
runtime dependencies. No Scala or sbt installation is needed, but Java 17 or newer
must be available on `PATH` (or configured through `JAVA_HOME`).

To build both archives locally:

```bash
sbt Universal/packageBin Universal/packageZipTarball
```

The resulting files are written to `target/universal/`.

### Maintainer Release

Update `version` in `build.sbt`, commit the change, and push an annotated tag whose
name is `v<version>` (for example, `v0.1.0`). The tag must match the sbt version. The
tag-triggered workflow runs tests and coverage, builds both archives, tests the
extracted packages on Java 17, 21, and 25, writes SHA-256 checksums, and publishes a
GitHub Release with the archives and checksums. It does not publish from ordinary
branch pushes or pull requests.

## Project Structure

```
src/
├── main/
│   ├── scala/inoxcal/
│   │   ├── Calendar.scala              # Core calendar logic
│   │   ├── CalendarTextRenderer.scala   # Plain and TamboUI calendar rendering
│   │   ├── InoxcalApp.scala            # Command-line interface
│   │   └── LocalizationManager.scala   # Internationalization support
│   └── resources/inoxcal/
│       ├── messages.properties         # English (default)
│       ├── messages_es.properties      # Spanish
│       └── messages_fr.properties      # French
└── test/scala/inoxcal/
    ├── CalendarTest.scala              # Unit tests for the Calendar class
    ├── LocalizationManagerTest.scala   # Tests for the resource bundles and i18n API
    ├── InoxcalAppTest.scala            # Tests for the command-line interface
    └── IntegrationTest.scala           # Integration tests and exact output snapshots
```

Build files: `build.sbt`, `scala.sbt` (compiler options), `project/plugins.sbt`
and `project/build.properties` (pinned sbt version). CI runs
`coverage test coverageReport` on JDK 25; `doc/ai/` holds the AI transcripts and
the remediation plan (`doc/ai/20260930_RemediationPlan.md`).

## API Reference

### Calendar Class

- `displayMonth(year, month)`: Display a specific month
- `displayMonth(year, month, startingDay, weekNumbers)`: Display a configured month
- `displayCurrentMonth()`: Display current month  
- `displayYear(year)`: Display entire year
- `displayYear(year, startingDay, weekNumbers)`: Display a configured year
- `displayCurrentYear()`: Display current year
- `getDaysInMonth(year, month)`: Get number of days in a month
- `isLeapYear(year)`: Check if a year is a leap year
- `getDayOfWeek(year, month, day)`: Get day of week for a date
- `monthView(year, month, startingDay, weekNumbers)`: Build structured month data

The companion object exposes the range accepted by the command-line interface:
`Calendar.MinYear`/`Calendar.MaxYear` (`1900`-`3000`) and
`Calendar.MinMonth`/`Calendar.MaxMonth` (`1`-`12`). Starting-day values use
`Calendar.MinStartingDay`/`Calendar.MaxStartingDay` (`0`-`6`).

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
- **Application Tests** (`InoxcalAppTest`): option parsing, validation failures through `Either`, and end-to-end output captured with `Console.withOut`
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
2. Update the `InoxcalApp` object for command-line interface changes
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

## Warranty

**Guaranteed rust-free.** *Inox* is short for *inoxidable*, Castilian for "stainless
steel": the word behind the "Inox" stamp on a kitchen sink and in *acero
inoxidable*. Inoxcal is therefore structurally stainless - it is written in Scala,
not Rust, so the only thing in this repository that can oxidize is the reader's
understanding of leap-year rules. There is no borrow checker, but you may borrow
any day of the month you like, as long as you give it back before midnight. The
[MIT license](LICENSE) covers everything except genuine corrosion: should your copy
develop rust, please file an issue with the detected oxide and we will prescribe
`sbt clean compile`.

## AI Disclosure

This project was developed with assistance from generative and agentic AI tools,
including [Cline](https://cline.bot/). AI assistance was used for brainstorming, implementation,
documentation, and code review. Human contributors reviewed and tested the
resulting work and remain responsible for the final contents of this repository.
