# AGENTS.md

Guidance for agents and contributors working on Inoxcal.

## Project map

- `src/main/scala/inoxcal/Calendar.scala`: date calculations and structured month/year data.
- `src/main/scala/inoxcal/CalendarTextRenderer.scala`: plain-text and TamboUI-backed rendering.
- `src/main/scala/inoxcal/InoxcalApp.scala`: `mainargs` parsing, validation, dispatch, help, and CLI effects.
- `src/main/scala/inoxcal/LocalizationManager.scala`: localized names, messages, and help text.
- `src/main/resources/inoxcal/messages*.properties`: English, Spanish, and French resource bundles.
- `src/test/scala/inoxcal/`: unit, integration, localization, CLI, and output-regression tests.
- `build.sbt`, `project/`: Scala, dependency, coverage, packaging, and release configuration.
- `.github/workflows/`: CI and tag-triggered release automation.
- `scripts/smoke-test-package.sh`: extracted ZIP/tar.gz archive smoke tests.
- `doc/ai/`: historical agent-session and remediation records; current code and configuration are authoritative.

## Coding conventions

- Use Scala 3 significant indentation; do not introduce brace-delimited Scala blocks.
- Prefer expression-oriented, immutable code and immutable public collections.
- Keep calendar data separate from its text or ANSI rendering.
- Keep validation pure and testable with `Either`; print errors and exit non-zero only at the CLI boundary.
- Use the constants in `Calendar` as the single source of truth for CLI bounds and related messages.
- Preserve the existing compiler warnings and strictness policy. Avoid unrelated dependency or toolchain changes.
- Follow existing names: `PascalCase` for types/objects and `camelCase` for methods and values.

## Compatibility and localization

- Preserve default behavior and output: current-month display with no arguments, Sunday-first weeks, and vertically stacked year views.
- The CLI accepts years `1900..3000`, months `1..12`, and starting-day values `0..6`. The `Calendar` API may support a broader proleptic Gregorian range.
- Reject simultaneous positional and named year arguments rather than inventing precedence.
- Preserve localized help, including the special handling needed when `--help` is the first argument.
- Update all English, Spanish, and French resource bundles together when adding or changing user-facing text.
- Preserve locale-independent tests by using explicit locales or the pinned test locale configured in `build.sbt`.
- Emit ANSI color only when output is attached to a terminal; redirected and captured output must remain plain text.
- Preserve the existing ISO week-number rule: label each row with the ISO week of its last date in the row.

## Validation

Run the relevant checks after changes:

```bash
sbt clean compile
sbt test
sbt coverage test coverageReport
```

Coverage must remain at least **90% statement** and **85% branch** coverage. For CLI or packaging changes, also run representative checks:

```bash
sbt "runMain inoxcal.InoxcalApp --help"
sbt "runMain inoxcal.InoxcalApp 2024"
sbt "runMain inoxcal.InoxcalApp --month 3 --year 2024 --starting-day 1 --week-numbers"
sbt "runMain inoxcal.InoxcalApp --month 13"
```

For release-package changes, build both Universal archives and run `scripts/smoke-test-package.sh` against the resulting ZIP and tar.gz files. Add regression tests for changed behavior, including boundary cases and exact output where output compatibility matters. Report actual command results; do not substitute historical results for current verification.

## Workflow and documentation

- Keep changes focused and update `README.md` examples and feature descriptions when CLI behavior changes.
- Treat current source and build files as authoritative when they differ from historical `doc/ai/` records.
- Preserve historical AI transcripts unless explicitly asked to rewrite them. Do not edit generated `.bloop`, `target`, or IDE state as part of source changes.
- Releases use the configured `sbt-release` process and the tag-triggered `Tagged Release` GitHub Actions workflow; there is no standalone release script.
- Before releasing, verify the tag matches `version.sbt`, inspect the release commit, and push only the intended branch and tag. Do not use `git push --tags` casually.
- For remote operations, distinguish a completed and verified result from an attempted command or an inconclusive tool response.