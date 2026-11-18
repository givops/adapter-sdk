# GivOps adapter SDK

Public Java contracts for independently installed GivOps ticketing and forge libraries. Build adapters without access to the private GivOps core. Licensed under Apache-2.0; see [LICENSE](LICENSE).

## Build

Install Java 25 and Maven 3.9+, then run:

```sh
mvn -B clean install
mvn -B -f examples/example-ticketing/pom.xml clean verify
```

No database, private repository, credentials or live provider is required. Dependencies are resolved from the public Maven repositories configured by the Spring Boot parent. The supported baseline is Java 25, Spring Boot 4.0.9 and Jackson 3. This repository currently produces `0.5.0-SNAPSHOT` artifacts; do not assume they have been uploaded to Maven Central.

Use `io.givops:adapter-spring-boot:0.5.0-SNAPSHOT` for adapters and `io.givops:adapter-testkit:0.5.0-SNAPSHOT` in test scope. The five modules are `adapter-common`, `adapter-ticketing-api`, `adapter-forge-api`, `adapter-spring-boot` and `adapter-testkit`. Ticketing and forge contracts are separate domains in one SDK; the SDK itself is not a provider.

## Write a provider

The [example](examples/example-ticketing) is a separate build with provider ID `EXAMPLE_TICKETS`. It registers a descriptor, credential fields, read adapter and optional capability inspector through Spring Boot `AutoConfiguration.imports`. It keeps observation acquisition metadata-only and independently exposes a synthetic browser with pagination, detail, revision-bound Markdown and attachments. It makes no live HTTP calls. Copy the structure, replace its provider implementation, and declare only capabilities you implement.

Publish an artifact-owned `META-INF/givops-adapter.properties` containing `adapterId`, `provider`, `adapterVersion`, `sdkApiVersion` and `domains` (a comma-separated list such as `ticketing,forge`). Register the descriptor independently of the adapter's credential service to avoid a host/definition dependency cycle. See the exact contracts in [CONTRACT.md](CONTRACT.md).

Adapters receive opaque credential leases and a signing service. The host retains encryption keys, HMAC secrets, persistence and transaction locks. Do not log, serialize or retain credentials beyond the operation. Check the lease again at the publication boundary; host-managed locked revalidation must remain inside the transaction.

Each artifact owns its message facade and explicitly named YAML resources. The SDK formatter implements strict ICU MessageFormat 1 with English fallback; never depend on the core's private catalogs. Keep provider API syntax and user data outside translated templates.

## Install or replace a provider

Build a plain library JAR, then place it in the host adapter directory and restart GivOps. The container path is `/opt/givops/adapters`; a read-only bind mount replaces that directory completely. The core application JAR does not embed provider libraries. `LOADER_PATH` selects the adapter directory for the PropertiesLauncher runtime. Install one version of each adapter; duplicate artifacts, competing registrations for the same provider/domain, or incompatible SDK API levels are startup errors. One library may register the same provider in both ticketing and forge. Required extra provider dependencies must be supplied alongside the provider, without bundling another SDK or framework version.

The host lists installed descriptors at `GET /api/v2/catalog/ticketing-providers` and `GET /api/v2/forge/providers`. Each supplies the installed provider identity, capabilities, API profiles and credential fields. Forge descriptors also define hosting choices, repository validation and an optional commit-link path template; ticketing descriptors provide command labels and project validation. Hosted ticketing and forge providers use `ADAPTER_API` credentials with `{ "fields": { "apiToken": "..." } }`; provider identity comes from the bound instance. Removing a provider makes its operations unavailable while retaining historical catalog and observation data.

[Compatibility](docs/compatibility.md) distinguishes SDK compatibility, artifact versions, product versions and API dialects. [Contributing](CONTRIBUTING.md) explains the standalone checks and release artifacts.

## Providers with both domains

One vendor library can register a `TicketProviderDefinition` and a
`ForgeProviderDefinition`, plus the capabilities it implements. GitLab and GitHub
use this arrangement; Jira and YouTrack provide ticketing. Share vendor HTTP,
version and authentication code within that library. Either domain can be used
independently. Register the definitions without depending on a credential host
bean, then inject the narrow public host services into operation implementations.

Keep the provider's URL syntax, authentication construction, pagination, payload
mapping and remote API-version choices in the adapter. GivOps owns authorization,
credential encryption/leases, transactions, scheduling and stored observations.
The adapter must not depend on the private application or its fixtures. Public
HTTP fixtures and the test kit let contributors build and verify a library alone.

Native numeric issue references must include their project/repository scope in
identity and routing. A bare `#123` is resolved against the selected bindings;
`owner/repository#123` and provider URLs can identify a more specific scope.
Implement the definition's reference normalization rather than manufacturing a
Jira-style key. Milestones retain their native state and dates; they are not Git
releases or tags. Declare each optional operation only when implemented.


## Local verification before merging

Run `./scripts/verify-local.sh` on the final source with Java 25 and Maven 3.9+.
It builds the public SDK, independent example, and release bundle locally; no provider account or private
repository is required. Record the command and results before merging each PR.
Build and test verification runs locally before merging. GitHub Actions runs
package releases where configured and automatic CodeQL security scans.
`GIVOPS_MAVEN_REPOSITORY` selects an isolated Maven cache (default `.m2-local/`).
Do not share a cache between concurrent snapshot builds.
