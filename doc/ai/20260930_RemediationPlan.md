# Scalendar remediation plan (2026-09-30)

This document records the findings of a review of `scalendar-scala` and the
plan that was agreed to address them. It complements
`doc/ai/20260827_QualityReview.md`, which is an automated (GitHub Copilot)
quality review of an earlier revision; some of the issues raised there were
already fixed by commit `376535b` ("removed redundant code based on AI review"),
so the items below reflect the current state of the tree at `738e42a` plus the
findings that were still open.

Scope decisions:

- Behaviour stays backwards compatible: the CLI accepts the same options and
  produces the same calendar output (plus a localized help header/footer).
- The year range accepted by the CLI stays `1900..3000`.
- `scalaVersion` stays in `scala.sbt` (see "Deviations" below).

## Findings (current tree)

| # | Category | Finding | Where | Severity |
|---|---|---|---|---|
| F1 | Portability | Tests depend on the JVM default locale: they assert English month/day names while `Calendar` defaults to `Locale.getDefault`, so they fail on a non-English machine | `CalendarTest.scala`, `IntegrationTest.scala`, `ScalendarAppTest.scala` | High |
| F2 | Correctness | Validation bounds are duplicated as literals in code *and* in prose inside three bundles, so they can drift | `ScalendarApp.scala`, `messages*.properties` | High |
| F3 | Consistency | `Calendar` is unbounded (year 1 works) while the CLI rejects years outside `1900..3000`; `IntegrationTest` documents neither | `IntegrationTest.scala` | Medium |
| F4 | Ergonomics | Validation errors are printed to stdout and `sys.exit(1)` is called inside the validators, so the failure paths cannot be tested | `ScalendarApp.scala` | Medium |
| F5 | Dead code | `help.*` keys are translated in all three bundles but `getHelpDescription`/`getUsageText`/`getExamplesText` were never called; `--help` printed English mainargs output only. `help.examples` additionally documented positional arguments that mainargs rejects | `LocalizationManager.scala`, `messages*.properties` | Medium |
| F6 | API | `getAllDayNames`/`getAllMonthNames` returned mutable `Array[String]`; `getAllMonthNames`, `getCurrentLocale`, `forLocale` were untested | `LocalizationManager.scala` | Medium |
| F7 | Style | Multiple `return`s in `scalendar(...)`; two `Calendar`/`LocalizationManager` instances were allocated when `--locale` was used | `ScalendarApp.scala` | Low |
| F8 | Tests | Vacuous test (`Option` semantics), duplicated help-text tests, alignment assertion with no lower bound, no golden output test, no test invoked `ScalendarApp.scalendar(...)` end to end | `*Test.scala` | Medium |
| F9 | Build | `sbt-scoverage` runs in CI but there is no coverage gate | `build.sbt` | Low |
| F10 | Docs | `README.md` had two "## Features" sections, did not document `--year-view` precedence, overstated automatic localization (help was English only), and omitted `scala.sbt`, `doc/ai/`, and the license | `README.md` | Low |
| F11 | Hygiene | `doc/ai/20260827_QualityReview.{md,json}` are untracked; the JSON is 376 KB of agent logs | `doc/ai/` | Low |
| F12 | Dead code | mainargs intercepts a leading `--help` (`Parser.runEither`: `args.take(1) == Seq("--help")` prints its own English help and exits 0), so the `help.value` branch of `scalendar(...)` was unreachable from the CLI and `--help` was English-only no matter the locale | `ScalendarApp.scala` | Medium |

Explicitly out of scope: `cal`-style three-months-per-row year view, centering
month headers for variable-length month names, trimming the review JSON, and
moving `scalaVersion` into `build.sbt`.

## Plan

### P0 - determinism and a single source of truth

- [x] P0.1 Run tests in a forked JVM with a pinned locale
      (`Test / fork := true`, `Test / javaOptions ++= Seq("-Duser.language=en",
      "-Duser.country=US")`) so default-locale code paths (e.g.
      `new LocalizationManager()`) are stable in CI and on any developer machine.
- [x] P0.2 Use an explicitly English calendar (`Calendar.withLanguage("en")`) in
      the suites that assert English month/day names.
- [x] P0.3 Introduce `Calendar.MinYear/MaxYear/MinMonth/MaxMonth` as the single
      source of truth and use them in `validateMonth`/`validateYear` and in the
      localized error messages (now `{0}` = value, `{1}` = minimum, `{2}` = maximum).

### P1 - testability and API cleanliness

- [x] P1.1 `validateMonth`/`validateYear` return `Either[String, Unit]`; the
      caller prints to `System.err` and exits non-zero. Failure paths become unit
      testable and errors no longer go to stdout.
- [x] P1.2 Remove the `return`s in `scalendar(...)`: the whole dispatch is one
      expression producing `Either[String, String]` that is printed once.
- [x] P1.3 Build exactly one `Calendar` per invocation
      (`locale.fold(new Calendar)(Calendar.withLanguage)`).
- [x] P1.4 Wire the localized help keys into the `--help` path
      (`localizedHelp`: translated description + mainargs option list +
      examples). Removed the unused and duplicated `help.usage` key and corrected
      the examples to mainargs syntax (`--month 3 --year 2024`, not `3 2024`).
      Because mainargs intercepts a leading `--help` (F12), `main(args)` now prints
      the localized help itself in that case and only delegates to
      `ParserForMethods.runOrExit` otherwise; the locale is read from `-l`,
      `--locale`, or `--locale=`. `-h` and a non-leading `--help` still go through
      the parser and the `help` flag.
- [x] P1.5 `getAllDayNames`/`getAllMonthNames` return `IndexedSeq[String]`.
- [x] P1.6 Added tests for `getAllMonthNames`, `getCurrentLocale`,
      `LocalizationManager.forLocale`, `Calendar.withLocale` (chosen over
      deleting the demo API documented in the README).

### P2 - strengthen the suite

- [x] P2.1 Alignment assertion tightened to `shouldBe 20` (every grid row is
      7 two-char cells joined by single spaces).
- [x] P2.2 Deleted the vacuous `Option` test and merged the two duplicated
      help-text tests into one.
- [x] P2.3 Relabelled the year test to reflect actual semantics: `Calendar` is
      unbounded, the CLI range is `1900..3000` (asserted through the new `Either` API).
- [x] P2.4 Added exact-string ("golden") tests for March 2024 and for the year
      view structure.
- [x] P2.5 Added end-to-end tests that capture `ScalendarApp.scalendar(...)`
      output with `Console.withOut` (default, month+year, year view, help, `--locale`).
- [x] P2.6 Added invalid-input tests through the `Either` API (month 0/13,
      year 1899/3001) asserting both the `Left` and the localized message.
- [x] P2.7 Added a new `LocalizationManagerTest` covering bundle loading in all
      three locales, unsupported-locale fallback (`de` -> English),
      `forLanguage`/`forLocale`/`getCurrentLocale`, and the error-message API.

### P3 - build, docs, hygiene

- [x] P3.1 Added a coverage gate (`coverageMinimumStmtTotal`,
      `coverageFailOnMinimum`) so the CI `coverageReport` step fails on regressions.
- [ ] P3.2 Commit the untracked `doc/ai/20260827_QualityReview.*` files
      (recommendation only - left to the repository owner, see "Follow-ups").
- [x] P3.3 README: merged the duplicated "Features" sections into an
      "API reference" section, documented `--year-view` precedence, corrected the
      localization claims, fixed the "add a language" example, and added the
      missing tooling/license/structure information.

## Deviations from the review

- `scalaVersion` stays in `scala.sbt`: sbt merges all root `*.sbt` files, the
  split is deliberate in this repository, and moving it would churn tooling
  caches for no functional gain. Documented in the README instead.
- `getCurrentLocale`/`forLocale`/`Calendar.withLocale` were kept and tested
  rather than deleted, because the README advertises them as demo API.
- The automated review's notes about `Calendar.scala:17` (`val date`) and unused
  `DateTimeFormatter`/`WeekFields` imports refer to a revision that predates
  commit `376535b`; those are already gone. Its claim that `-Yexplicit-nulls` is
  "not enforced" is inaccurate: Java interop types are *flexible*
  (`T | Null`), which is why the code compiles without `.nn`, and
  `ResourceBundle.getString` throws `MissingResourceException` rather than
  returning null.

## Verification

- `sbt test`
- `sbt coverage test coverageReport`
- `sbt "runMain scalendar.ScalendarApp --month 3 --year 2024"`
- `sbt "runMain scalendar.ScalendarApp --locale es --month 3 --year 2024"`
- `sbt "runMain scalendar.ScalendarApp --help"`

## Follow-ups

- Commit `doc/ai/20260827_QualityReview.{md,json}` or add the (large) JSON to
  `.gitignore`; consider truncating it to the summary to keep the clone small.
- Optional feature work: three-months-per-row year view and header centering.

## Outcome

- Test suite: **45 tests, all passing** (was 30 before this work), in
  `CalendarTest` (13), `IntegrationTest` (8), `LocalizationManagerTest` (9),
  `ScalendarAppTest` (15).
- Coverage: **96.39% statements / 96.15% branches** (`sbt coverage test
  coverageReport`); the gate is set to 90% / 85%.
- CLI smoke tests: `--month 3 --year 2024` matches the README golden output,
  `--locale es --month 3 --year 2024` prints `Marzo 2024` with `Do Lu Ma Mi Ju Vi Sa`,
  `--help`/`-h` print the localized description, the option list and the examples,
  `--help --locale es` prints the Spanish description, and `--month 13` prints
  `Invalid month: 13. Month must be between 1 and 12.` on stderr and exits `1`.
- Files changed: `build.sbt`, `Calendar.scala`, `LocalizationManager.scala`,
  `ScalendarApp.scala`, the three `messages*.properties` bundles, `README.md`,
  all four test sources (one of them new), plus this document.
