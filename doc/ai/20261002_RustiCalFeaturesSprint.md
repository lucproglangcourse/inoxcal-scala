# Rusti-cal feature sprint (2026-10-01)

This document records the implementation of the approved rusti-cal-inspired
features for inoxcal. The sprint adds positional year input, configurable week
starts, ISO week numbers, and optional TamboUI styling while preserving
inoxcal's existing defaults and layout.

## Objective

Add the following rusti-cal-style command-line capabilities:

```text
inoxcal [<year>] [--starting-day <starting-day>] [-c] [-w]
```

The approved compatibility decision was to keep inoxcal's current behavior when
no new option is supplied:

- no arguments display the current month;
- `--year` and `--year-view` continue to display a vertically stacked year;
- existing `--month`, `--year`, `--locale`, and help behavior remain available;
- the current validation range remains years `1900..3000` and months `1..12`;
- no three-months-per-row layout or interactive TUI navigation is introduced.

## Features implemented

### Positional year

`inoxcal 2024` is equivalent to `inoxcal --year 2024` and displays the full
year. The parser uses mainargs' `@arg(name = "year", positional = true, ...)`
annotation.

Supplying both forms is rejected:

```text
inoxcal 2024 --year 2025
```

This avoids undocumented precedence between two explicit year values.

### Starting weekday

`--starting-day` accepts integers `0..6`:

| Value | Weekday |
|---:|---|
| 0 | Sunday |
| 1 | Monday |
| 2 | Tuesday |
| 3 | Wednesday |
| 4 | Thursday |
| 5 | Friday |
| 6 | Saturday |

The default is `0`, preserving the existing Sunday-first output. The weekday
header and date columns rotate together; dates outside the selected month remain
empty cells rather than being displayed as neighboring-month dates.

### Week numbers

`-w` and `--week-numbers` add a `Wk` column. Each row is labeled using the ISO
week number of the last date in that displayed row, matching the approved
rusti-cal-compatible rule for rows that can span ISO weeks.

Example:

```text
    March 2024
Wk Mo Tu We Th Fr Sa Su
 9              1  2  3
10  4  5  6  7  8  9 10
11 11 12 13 14 15 16 17
12 18 19 20 21 22 23 24
13 25 26 27 28 29 30 31
```

### TamboUI color

`-c` and `--color` enable styled output. TamboUI `0.5.0` is used through its
published `tamboui-core` artifact. The renderer uses TamboUI's public
`Style`, `Span`, `Line`, `Text`, and `AnsiStringBuilder` APIs.

Styles are assigned as follows:

- month heading: bold cyan;
- Sunday dates: red;
- Saturday dates: yellow;
- current date: reversed/bold;
- week numbers: magenta;
- weekday heading: bold.

The command checks `System.console()` before emitting colored output. When
stdout is not attached to a real terminal—for example, in a test capture or a
redirect—the `--color` flag falls back to plain text. This keeps redirected
output free of ANSI control sequences while still using TamboUI for the styled
terminal path.

The dependency was initially checked against the documented `0.6.0-SNAPSHOT`
API, but that snapshot coordinate was not available through the configured
resolution path. The implementation therefore uses the published `0.5.0`
`dev.tamboui:tamboui-core` artifact, whose JAR was inspected locally and
verified to contain the required APIs.

## Implementation details

### Structured calendar model

`Calendar.scala` now exposes `MonthView` and `MonthWeek` data structures. A
month view contains:

- the localized month name;
- the rotated weekday sequence;
- date cells grouped into weeks;
- optional ISO week numbers;
- the localization manager used for rendering.

The existing `displayMonth(year, month)` and `displayYear(year)` methods remain
as default wrappers, so existing callers retain the original output.

### Renderer separation

`CalendarTextRenderer.scala` renders the same structured data in two ways:

1. plain text, preserving the existing output shape by default;
2. TamboUI-styled text converted to ANSI sequences only for terminal output.

The renderer does not parse already-formatted calendar strings to determine
where dates or styles belong.

### CLI and localization

`InoxcalApp.scala` now validates starting days and conflicting year sources,
then selects plain or TamboUI rendering. New localized resource keys were added
to all three bundles:

- `error.invalid.starting-day`;
- `error.conflicting.year`;
- updated examples for positional years, colors, starting days, and week numbers.

The generated help includes all new options.

## Tests added

The tests now cover:

- all valid starting-day values and invalid values;
- weekday rotation and date placement;
- ISO week-number output;
- TamboUI ANSI style generation;
- positional-year rendering;
- conflicting positional/named years;
- new help options;
- combined starting-day, week-number, and color flags.

The existing default-output snapshots remain in place and continue to pass.

## Verification

Initial verification after the implementation included:

- compilation with the published TamboUI dependency;
- the existing 45-test suite, which passed before the new tests were added;
- CLI help output;
- positional year output;
- Monday-first March 2024 with week numbers.

The final verification command for this sprint is:

```text
sbt clean compile
sbt test
sbt coverage test coverageReport
sbt "runMain inoxcal.InoxcalApp --help"
sbt "runMain inoxcal.InoxcalApp 2024"
sbt "runMain inoxcal.InoxcalApp --month 3 --year 2024 --starting-day 1 --week-numbers"
sbt "runMain inoxcal.InoxcalApp --month 13"
```

The final test and coverage results will be recorded below after the complete
verification run.

### Observed final results (2026-10-01)

- `sbt clean compile`: passed.
- `sbt test`: passed; 52 tests succeeded across 4 suites.
- `sbt coverage test coverageReport`: passed; 92.07% statement coverage and
  85.19% branch coverage.
- `sbt "runMain inoxcal.InoxcalApp --help"`: passed; help listed positional
  `year`, `--starting-day`, `--color`, and `--week-numbers`.
- `sbt "runMain inoxcal.InoxcalApp 2024"`: passed; rendered the full 2024
  year view.
- `sbt "runMain inoxcal.InoxcalApp --month 3 --year 2024 --starting-day 1 --week-numbers"`:
  passed; rendered `Wk Mo Tu We Th Fr Sa Su` with rows numbered 9 through 13.
- `git diff --check`: passed.

The test run emitted only the existing JDK warning about the deprecated
`sun.misc.Unsafe` API used by the Scala runtime.

## Follow-ups

- Consider adding a true terminal integration test for `--color`; current tests
  validate TamboUI ANSI generation directly and verify plain fallback through
  captured output.
- Consider adding explicit year-boundary tests for ISO week 52/53 behavior.
- No interactive TamboUI application or alternate-screen mode is required for
  this print-and-exit calendar command.