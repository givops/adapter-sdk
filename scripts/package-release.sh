#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p dist
for module in adapter-common adapter-forge-api adapter-ticketing-api adapter-spring-boot adapter-testkit; do
  cp "$module"/target/*.jar dist/
  cp "$module/pom.xml" "dist/$module.pom"
done
cp pom.xml dist/adapter-sdk.pom
cp examples/example-ticketing/target/adapter-example-ticketing-*.jar dist/
cp LICENSE README.md CONTRACT.md dist/
(cd dist && sha256sum -- *.jar *.pom LICENSE README.md CONTRACT.md > SHA256SUMS)
