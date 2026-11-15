# GivOps adapter SDK

Public Java contracts for independently installed GivOps ticketing libraries. Build adapters without access to the private GivOps core. Licensed under Apache-2.0; see [LICENSE](LICENSE).

## Build

Install Java 25 and Maven 3.9+, then run:

```sh
mvn -B clean install
mvn -B -f examples/example-ticketing/pom.xml clean verify
```

No database, private repository, credentials or live provider is required. Dependencies are resolved from the public Maven repositories configured by the Spring Boot parent. The supported baseline is Java 25, Spring Boot 4.0.9 and Jackson 3. This repository currently produces `0.1.0-SNAPSHOT` artifacts; do not assume they have been uploaded to Maven Central.

Use `io.givops:adapter-spring-boot:0.1.0-SNAPSHOT` for adapters and `io.givops:adapter-testkit:0.1.0-SNAPSHOT` in test scope. The four modules contain shared facilities, provider DTOs/ports, Boot configuration contracts, and synthetic contract fixtures respectively.

## Write a provider

The [example](examples/example-ticketing) is a separate build with provider ID `EXAMPLE_TICKETS`. It registers a descriptor, credential fields, read adapter and optional capability inspector through Spring Boot `AutoConfiguration.imports`. It returns synthetic empty projects and makes no live HTTP calls. Copy the structure, replace its provider implementation, and declare only capabilities you implement.

Publish an artifact-owned `META-INF/givops-adapter.properties` containing `adapterId`, `provider`, `adapterVersion` and `sdkApiVersion`. Register the descriptor independently of the adapter's credential service to avoid a host/definition dependency cycle. See the exact contracts in [CONTRACT.md](CONTRACT.md).

Adapters receive opaque credential leases and a signing service. The host retains encryption keys, HMAC secrets, persistence and transaction locks. Do not log, serialize or retain credentials beyond the operation. Check the lease again at the publication boundary; host-managed locked revalidation must remain inside the transaction.

Each artifact owns its message facade and explicitly named YAML resources. The SDK formatter implements strict ICU MessageFormat 1 with English fallback; never depend on the core's private catalogs. Keep provider API syntax and user data outside translated templates.

## Install or replace a provider

Build a plain library JAR, then place it in the host adapter directory and restart GivOps. The container path is `/opt/givops/adapters`; a read-only bind mount replaces that directory completely. The core application JAR does not embed provider libraries. `LOADER_PATH` selects the adapter directory for the PropertiesLauncher runtime. Install one version of each adapter; duplicate IDs/providers or incompatible SDK API levels are startup errors. Required extra provider dependencies must be supplied alongside the provider, without bundling another SDK or framework version.

The host lists installed descriptors at `GET /api/v2/catalog/ticketing-providers`. New providers use `ADAPTER_API` credentials with `{ "fields": { "apiToken": "..." } }`; provider identity comes from the bound instance. Removing a provider makes its operations unavailable while retaining historical catalog and observation data.

[Compatibility](docs/compatibility.md) distinguishes SDK compatibility, artifact versions, product versions and API dialects. [Contributing](CONTRIBUTING.md) explains the standalone checks and release artifacts.
