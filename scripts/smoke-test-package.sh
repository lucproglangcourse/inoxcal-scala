#!/usr/bin/env bash
set -euo pipefail

archive=${1:?usage: smoke-test-package.sh ARCHIVE}
workdir=$(mktemp -d)
trap 'rm -rf "$workdir"' EXIT

case "$archive" in
  *.zip)
    unzip -q "$archive" -d "$workdir"
    ;;
  *.tar.gz|*.tgz)
    tar -xzf "$archive" -C "$workdir"
    ;;
  *)
    echo "unsupported archive: $archive" >&2
    exit 2
    ;;
esac

launcher=$(find "$workdir" -type f -path '*/bin/inoxcal' -print -quit)
test -n "$launcher"
test -x "$launcher"
test -f "$(dirname "$(dirname "$launcher")")/README.md"
test -f "$(dirname "$(dirname "$launcher")")/LICENSE"

help_output=$("$launcher" --help)
grep -Fq 'Inoxcal' <<<"$help_output"
grep -Fq -- '--locale' <<<"$help_output"

year_output=$("$launcher" 2024)
grep -Fq '2024' <<<"$year_output"

spanish_output=$("$launcher" --month 3 --year 2024 --locale es)
grep -Fq 'Marzo' <<<"$spanish_output"

options_output=$("$launcher" --month 3 --year 2024 --starting-day 1 --week-numbers)
grep -Fq 'March 2024' <<<"$options_output"

if "$launcher" --month 13 >"$workdir/invalid.out" 2>"$workdir/invalid.err"; then
  echo 'invalid input unexpectedly succeeded' >&2
  exit 1
fi
grep -Fq 'Invalid month: 13' "$workdir/invalid.err"

"$launcher" --month 3 --year 2024 >"$workdir/plain.out"
if grep -q $'\033' "$workdir/plain.out"; then
  echo 'redirected output unexpectedly contains ANSI escape codes' >&2
  exit 1
fi

echo "package smoke test passed: $archive"