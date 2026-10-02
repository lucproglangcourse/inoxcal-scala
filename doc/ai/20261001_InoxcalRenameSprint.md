# Inoxcal rename sprint (2026-10-01)

This document records the complete `scalendar` to `inoxcal` rename sprint. The
rename was intentionally limited to project identity and namespace changes:
calendar behavior, command-line options, localization behavior, validation
bounds, and output formatting were preserved.

The related remediation work is documented in
`doc/ai/20260930_RemediationPlan.md`. That document contains the preceding
quality findings and remediation outcome; its final section, “Rename to
inoxcal,” provides a concise cross-reference for this sprint.

## Sprint objective

Rename the project consistently from `scalendar` to `inoxcal` across:

- Scala packages and source directories;
- the application object, entry point, and test class names;
- resource-bundle directories and lookup keys;
- build metadata and the packaged application name;
- command-line help and localized examples;
- README examples, source-tree documentation, and warranty text;
- the active remediation record.

The rename had to preserve the existing behavior and remain compatible with the
current Scala 3 / sbt project structure.

## Scope and decisions

### Included

- Main package: `scalendar` → `inoxcal`.
- Test package: `scalendar` → `inoxcal`.
- Main application: `ScalendarApp.scala` / `ScalendarApp` →
  `InoxcalApp.scala` / `InoxcalApp`.
- Mainargs entry point: `scalendar` → `inoxcal`.
- Resource bundle base name: `scalendar.messages` → `inoxcal.messages`.
- Build name and configured main class.
- Launcher and CLI naming as represented in build/package configuration.
- README commands, examples, source-tree diagrams, and project identity text.
- Test names and help-text assertions.
- Documentation of the rename and its verification.

### Excluded

- Historical AI session transcripts in `doc/ai/20250922.md` and
  `doc/ai/20260827_QualityReview.{md,json}`. Those files are historical records
  and intentionally retain the name that was current when each session occurred.
- Generated build metadata under `.bloop` and other generated/IDE state. These
  files can continue to contain paths derived from the repository directory or
  the previous sbt project identity and are not active source configuration.
- Functional redesign, output-format changes, new CLI options, or localization
  content changes unrelated to the application name.

## Before-and-after map

| Area | Before | After |
|---|---|---|
| Scala package | `scalendar` | `inoxcal` |
| Main source directory | `src/main/scala/scalendar/` | `src/main/scala/inoxcal/` |
| Test source directory | `src/test/scala/scalendar/` | `src/test/scala/inoxcal/` |
| Resource directory | `src/main/resources/scalendar/` | `src/main/resources/inoxcal/` |
| Main source file | `ScalendarApp.scala` | `InoxcalApp.scala` |
| Main object | `ScalendarApp` | `InoxcalApp` |
| Mainargs method | `@main def scalendar(...)` | `@main def inoxcal(...)` |
| Application test | `ScalendarAppTest` | `InoxcalAppTest` |
| Resource lookup | `ResourceBundle.getBundle("scalendar.messages", ...)` | `ResourceBundle.getBundle("inoxcal.messages", ...)` |
| Build name | `scalendar-scala` | `inoxcal-scala` |
| Configured main class | `scalendar.ScalendarApp` | `inoxcal.InoxcalApp` |
| CLI help header | `scalendar` | `inoxcal` |

## Implementation record

### 1. Namespace and source-tree rename

All main and test declarations were changed to `package inoxcal`. The source
directories were moved in parallel so that the filesystem layout matches the
package declarations:

```text
src/main/scala/inoxcal/
  Calendar.scala
  InoxcalApp.scala
  LocalizationManager.scala

src/test/scala/inoxcal/
  CalendarTest.scala
  InoxcalAppTest.scala
  IntegrationTest.scala
  LocalizationManagerTest.scala
```

No wildcard imports required updating because the project files use a single
package declaration rather than cross-package imports.

### 2. Application entry point

`ScalendarApp.scala` was renamed to `InoxcalApp.scala`, and the object became
`InoxcalApp`. Its mainargs method was renamed from `scalendar` to `inoxcal`.

This method name is observable: mainargs derives the command name shown in its
generated option list from the `@main` method. Consequently, the help output,
examples, and the parser assertion in `InoxcalAppTest` all had to change
together.

The explicit `main(args: Array[String])` dispatch method was retained. This
preserves the special handling for a leading `--help`, which is required for
localized help rather than mainargs' default English-only interception.

### 3. Resource bundles and localization

The three resource bundles were moved from the old resource directory to:

```text
src/main/resources/inoxcal/messages.properties
src/main/resources/inoxcal/messages_es.properties
src/main/resources/inoxcal/messages_fr.properties
```

`LocalizationManager` now resolves `inoxcal.messages`. The localized help
strings and examples were updated so that English, Spanish, and French help do
not mix the old application name with the new one.

### 4. Build configuration

`build.sbt` now contains:

```scala
name := "inoxcal-scala"
Compile / mainClass := Some("inoxcal.InoxcalApp")
```

The existing Scala version remains in `scala.sbt`, consistent with the prior
project arrangement. No dependency or plugin changes were needed for the
rename.

### 5. Tests

The application test was renamed to `InoxcalAppTest`, and its package and
references were updated. In particular, the parser help assertion now checks
for `inoxcal`.

The existing test suite was not weakened or bypassed. Calendar, integration,
localization, and CLI behavior continue to be exercised under the new package
and entry-point names.

### 6. README and project documentation

The README was updated to:

- use the `Inoxcal` project heading and identity;
- use `inoxcal.InoxcalApp` in all sbt commands;
- show the `inoxcal` command in examples and help-oriented text;
- update the source-tree diagram and test filenames;
- preserve the existing CLI behavior and validation documentation;
- retain the stainless-steel/inox warranty wordplay.

The source-tree diagram spacing was also corrected after the filename rename so
that the comments remain aligned with the shorter/longer new names.

The active remediation plan received a dated “Rename to inoxcal” section with
the before/after mapping, consequences of the mainargs rename, historical-file
policy, and verification outcome.

## Verification procedure

The following verification stages were run after the rename:

```text
sbt clean compile
sbt test
sbt coverage test coverageReport
sbt "runMain inoxcal.InoxcalApp --month 3 --year 2024"
sbt "runMain inoxcal.InoxcalApp --locale es --month 3 --year 2024"
sbt "runMain inoxcal.InoxcalApp --help"
sbt "runMain inoxcal.InoxcalApp --month 13"
```

The verification log is retained at `/tmp/inox_verify.log` for the current
working session.

## Verification results

### Compilation

- `sbt clean compile`: passed with exit code `0`.
- sbt reported the active project as `inoxcal-scala`.
- Three main Scala sources compiled successfully.

### Tests

- Total tests: **45**.
- Suites completed: **4**.
- Aborted suites: **0**.
- Failed tests: **0**.
- Canceled, ignored, and pending tests: **0**.
- Test command exit code: `0`.

The suites were:

- `CalendarTest`;
- `IntegrationTest`;
- `LocalizationManagerTest`;
- `InoxcalAppTest`.

### Coverage

- Statement coverage: **96.39%**.
- Branch coverage: **96.15%**.
- Coverage report generation: passed.
- Coverage command exit code: `0`.

The configured coverage gate remains lower than the measured result:
90% statement coverage and 85% branch coverage.

### CLI smoke tests

- English March 2024 output completed successfully.
- Spanish March 2024 output completed successfully and included `Marzo 2024`
  and `Do Lu Ma Mi Ju Vi Sa`.
- `--help` completed successfully and displayed the `Inoxcal` description,
  `inoxcal` command header, options, and examples.
- Invalid month `13` printed
  `Invalid month: 13. Month must be between 1 and 12.` to stderr and exited
  with status `1`, as intended.

## Consistency and hygiene checks

The active source, build, README, and current remediation documentation were
checked for stale `scalendar` references. No active source or configuration
reference remains.

Expected historical/generated references remain in:

- the historical AI transcripts, which were intentionally not rewritten;
- generated `.bloop` metadata, which is not project source or active build
  configuration;
- the rename tables and historical explanations in this documentation and the
  remediation plan, where the old name is necessary to describe the migration.

The working tree has not been committed. The expected rename shape appears as
deleted old paths, new `inoxcal` paths, and modifications to `README.md`,
`build.sbt`, and the active remediation document.

## Outcome

The rename sprint is complete. The project now presents itself as `inoxcal` at
the package, source-tree, resource, build, entry-point, test, CLI, README, and
active-project-documentation levels.

Behavior was preserved, including:

- month and year rendering;
- year-view precedence;
- locale selection and fallback;
- localized help;
- validation ranges;
- stderr/error exit behavior;
- calendar formatting and integration output.

The final verification run confirms that the renamed project compiles, all 45
tests pass, coverage remains above the configured gate, and the renamed CLI
works in English, Spanish, help, and invalid-input paths.

## Follow-ups

- Review whether generated `.bloop` metadata should be regenerated or ignored in
  a future repository-hygiene pass; it is unrelated to the functional rename.
- Decide separately whether the historical AI transcript files should be
  committed, summarized, or excluded from future documentation changes.
- No additional rename work is required for the active source tree.