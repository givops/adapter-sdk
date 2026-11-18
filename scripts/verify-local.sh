#!/usr/bin/env bash
set -euo pipefail
[[ $# == 0 ]] || { echo 'Usage: scripts/verify-local.sh' >&2; exit 2; }
verification_log=$(mktemp)
trap 'rm -f "$verification_log"' EXIT
run_verification() {
  repo_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
  cache=${GIVOPS_MAVEN_REPOSITORY:-"$repo_root/.m2-local"}
  [[ "$cache" = /* ]] || cache="$PWD/$cache"
  cd "$repo_root"
  # Include the independently built example and the downloadable bundle.
  mvn -B --no-transfer-progress -Dmaven.repo.local="$cache" clean install
  mvn -B --no-transfer-progress -Dmaven.repo.local="$cache" -f examples/example-ticketing/pom.xml clean verify
  scripts/package-release.sh
  git diff --check
}
run_verification "$@" 2>&1 | tee "$verification_log"
if grep -Eq 'Tests (are )?skipped|Tests run:.*Skipped: [1-9][0-9]*|OK \(skipped=[1-9]' "$verification_log"; then
  echo 'Local verification requires every test to run; skipped tests were reported.' >&2
  exit 1
fi
