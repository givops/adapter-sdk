# Adapter SDK 0.1 contract

Java 25, Spring Boot 4.0.9 and its Jackson 3 baseline. Public types retain their existing package names to avoid unnecessary model remapping. All public modules build without GivOps private source.

Providers depend on `io.givops:adapter-spring-boot:0.1.0-SNAPSHOT`. It exposes `adapter-ticketing-api` and `adapter-common` transitively. Use Boot `AutoConfiguration.imports` to register a `TicketProviderDefinition` bean and capability beans (`TicketProviderAdapter`, optional `TicketWriteAdapter`, `TestReportProviderAdapter`, `ReleaseWriteAdapter`). Declare only implemented capabilities. Bean definitions must work without component scanning from the host. Publish the definition independently of runtime credential availability.

The host supplies `io.givops.adapters.CredentialHost`, `ContextSigner`, `Clock`, `JsonMapper`, `TicketProperties` and `TestReportProperties`. Adapter configuration may use its own public `@ConfigurationProperties` types. Host services never import provider concrete types.

`CredentialHost.capture(CredentialRequest)` returns `CredentialLease` fields and destination pins. The lease contains the normalized host access revision and preserves locked revalidation through `requireCurrent(boolean)`. Never log a lease or fields. Use `ContextSigner.sign(provider, namespace, parts)` with the same ordered strings as the old connection HMAC; namespace is prepended by the signer, so do not repeat it in parts. The host retains the context secret. No adapter may depend on private `io.givops.credentials` classes.

`TicketModels.TicketProvider` is an open string-backed identity, preserving constant values and `name()/valueOf()`. Compare provider identities with `equals`; unknown IDs are not globally interned. Write ports use `TicketWriteModels.RemoteProjectRef`, not private catalog classes. Provider validation and runtime credentials are supplied through `TicketProviderDefinition`.

Capability IDs initially used: `ticketing.read`, `ticketing.version-membership`, `ticketing.write`, `ticketing.reports`, `ticketing.release-write`. API profiles describe actual implemented provider dialects; no server compatibility range may be invented. `TicketCapabilityInspector` is optional and read-only; absent inspection means UNKNOWN. SDK, adapter artifact, provider product version and API profile are distinct.

Core endpoint: GET `/api/v2/catalog/ticketing-providers` returns `{items:[{provider,displayName,adapterId,adapterVersion,capabilities,apiProfiles,credentialFields,projectKeyPattern,externalIdPattern}]}`. Credential fields use `{name,label,type,required,maxLength}`, type TEXT or SECRET. New providers use generic ADAPTER_API credential payload `{fields:{key:value}}`; the host determines identity from the bound target.

## Frozen API additions (0.1)

`TicketProviderAdapter.current(InstanceSource, Access, boolean locked)` is required; implement it explicitly using the captured host lease. A context-only comparison is insufficient for locked publication.

- `TicketProviderDefinition.sdkApiVersion(): String` defaults to `"0.1"`. The host calls `SdkCompatibility.requireCompatible(String)` and accepts this exact API level. This replaces the draft `sdkMajor()`; adapter artifact versions remain independent.
- `CredentialHost.capture(CredentialRequest)` and `CredentialLease.requireCurrent(boolean)` report expected host failures as `io.givops.adapters.CredentialAccessException`, with a public no-argument constructor and no private cause. Capture maps to provider `MISSING_CREDENTIALS`; revalidation maps to `SUPERSEDED`. Unexpected runtime failures are not caught indiscriminately.
- `io.givops.adapters.i18n.MessageCatalog.load(Class<?> resourceOwner, String absoluteResourcePath)` loads one explicit artifact-owned YAML resource. `load(Map<String, InputStream>)`, `format(String, Map<String, ?>)` and `patterns()` are also public. Each adapter owns its facade and a unique resource path, for example `/io/givops/adapters/jira/i18n/en.yml`. Never import private `io.givops.i18n.Messages` or package the core catalog/index. English fallback, strict MF1 and cloned concurrent formatters are preserved. Provider queries are protocol constants, not translated messages.
- `ReleaseWriteAdapter.WriteFailure(String code, boolean uncertain, String safeMessage)` permits adapter-owned sanitized text; the two-argument constructor remains available with an SDK message.
- `ProviderVersion.parse(String): Optional<ProviderVersion>` compares numeric components, with unknown vendor syntax remaining unknown. `ProviderVersionRange(lowerInclusive, upperExclusive).contains(version)` does not infer capability availability. The provider must inspect actual scoped evidence.
- `io.givops.adapters.testkit.AdapterTestkit.signer(String)` implements the documented length-prefixed HMAC using a synthetic key. `lease(Map<String,String>, Map<String,String>, String)` returns `MutableCredentialLease`, with `invalidate()` and `lastLocked()` for freshness tests. `verifyDefinition(TicketProviderDefinition)` checks descriptor basics and API compatibility.

An absent adapter makes refresh/remote writes unavailable without deleting historical observations. Duplicate providers/adapters and incompatible API levels fail host startup. Inspection is explicit and read-only, scoped to instance/project/captured credentials; ordinary reads never require a metadata call.

Credential descriptors declare at most 32 fields. Omit empty optional fields from the supplied fields map; retain nonempty values verbatim until provider validation normalizes them. The host does not echo generic credential values in metadata.
