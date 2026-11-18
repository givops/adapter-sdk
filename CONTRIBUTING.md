# Contributing

Java 25 and Maven 3.9+ are sufficient. No private GivOps repository, internal agent policy or account is needed.

Run `mvn -B clean install` and `mvn -B -f examples/example-ticketing/pom.xml clean verify`. Keep API changes documented in CONTRACT.md. An incompatible experimental API change requires a new exact API level and migration notes; do not silently broaden compatibility.

Use synthetic credentials and bounded transport fixtures. Test destination pinning, missing credentials, locked freshness, cancellation/budgets and uncertain remote writes as applicable. `AdapterTestkit.signer` implements real deterministic length-prefixed HMAC using a test-only key; its mutable lease supports stale-context tests. Never use these fixtures as production credential services.

SDK messages and adapter catalogs must contain only their own authored prose. Preserve strict YAML loading, named MF1 arguments, English fallback, quoting and concurrent formatting. Provider queries, protocol fields, issue titles and other external data must not become translation templates.

Run `scripts/verify-local.sh` before merging to build the SDK and independent example locally. Run `scripts/package-release.sh` after successful checks to collect the plain libraries, POMs, source JARs and SHA-256 manifest in `dist/`. Release publication is an explicit maintainer operation; the script does not upload anything. Review artifact contents before publishing and never include private core sources, credentials or build caches.
