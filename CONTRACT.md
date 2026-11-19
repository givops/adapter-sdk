# Adapter SDK 0.5 contract

Java 25, Spring Boot 4.0.9 and its Jackson 3 baseline. Public types retain their existing package names to avoid unnecessary model remapping. All public modules build without GivOps private source.

Providers depend on `io.givops:adapter-spring-boot:0.5.0-SNAPSHOT`. It exposes `adapter-ticketing-api`, `adapter-forge-api` and `adapter-common` transitively. Use Boot `AutoConfiguration.imports` to register a `TicketProviderDefinition` bean and capability beans (`TicketProviderAdapter`, optional `TicketWriteAdapter`, `TestReportProviderAdapter`, `ReleaseWriteAdapter`). Declare only implemented capabilities. Bean definitions must work without component scanning from the host. Publish the definition independently of runtime credential availability.

The host supplies `io.givops.adapters.CredentialHost`, `ContextSigner`, `Clock`, `JsonMapper`, `TicketProperties` and `TestReportProperties`. Adapter configuration may use its own public `@ConfigurationProperties` types. Host services never import provider concrete types.

`CredentialHost.capture(CredentialRequest)` returns `CredentialLease` fields and destination pins. The lease contains the normalized host access revision and preserves locked revalidation through `requireCurrent(boolean)`. Never log a lease or fields. Use `ContextSigner.sign(provider, namespace, parts)` with deterministic ordered strings; the host binds the provider and namespace, so do not repeat them in parts. The host retains the context secret. No adapter may depend on private `io.givops.credentials` classes.

`TicketModels.TicketProvider` is an open string-backed identity, preserving constant values and `name()/valueOf()`. Compare provider identities with `equals`; unknown IDs are not globally interned. Write ports use `TicketWriteModels.RemoteProjectRef`, not private catalog classes. Provider validation and runtime credentials are supplied through `TicketProviderDefinition`.

Capability IDs initially used: `ticketing.read`, `ticketing.version-membership`, `ticketing.project-create`, `ticketing.version-create`, `ticketing.issue-create`, `ticketing.reports`, `ticketing.release-write`. API profiles describe actual implemented provider dialects; no server compatibility range may be invented. `TicketCapabilityInspector` is optional and read-only; absent inspection means UNKNOWN. SDK, adapter artifact, provider product version and API profile are distinct.

Core endpoint: GET `/api/v2/catalog/ticketing-providers` returns `{items:[{provider,displayName,adapterId,adapterVersion,capabilities,apiProfiles,credentialFields,projectKeyPattern,externalIdPattern}]}`. Credential fields use `{name,label,type,required,maxLength}`, type TEXT or SECRET. New providers use generic ADAPTER_API credential payload `{fields:{key:value}}`; the host determines identity from the bound target.

## Earlier frozen additions (introduced in 0.2)

`TicketProviderAdapter.current(InstanceSource, Access, boolean locked)` is required; implement it explicitly using the captured host lease. A context-only comparison is insufficient for locked publication.

- `TicketProviderDefinition.sdkApiVersion(): String` defaults to `"0.5"`. The host calls `SdkCompatibility.requireCompatible(String)` and accepts this exact API level. This replaces the draft `sdkMajor()`; adapter artifact versions remain independent.
- `CredentialHost.capture(CredentialRequest)` and `CredentialLease.requireCurrent(boolean)` report expected host failures as `io.givops.adapters.CredentialAccessException`, with a public no-argument constructor and no private cause. Capture maps to provider `MISSING_CREDENTIALS`; revalidation maps to `SUPERSEDED`. Unexpected runtime failures are not caught indiscriminately.
- `io.givops.adapters.i18n.MessageCatalog.load(Class<?> resourceOwner, String absoluteResourcePath)` loads one explicit artifact-owned YAML resource. `load(Map<String, InputStream>)`, `format(String, Map<String, ?>)` and `patterns()` are also public. Each adapter owns its facade and a unique resource path, for example `/io/givops/adapters/jira/i18n/en.yml`. Never import private `io.givops.i18n.Messages` or package the core catalog/index. English fallback, strict MF1 and cloned concurrent formatters are preserved. Provider queries are protocol constants, not translated messages.
- `ReleaseWriteAdapter.WriteFailure(String code, boolean uncertain, String safeMessage)` permits adapter-owned sanitized text; the two-argument constructor remains available with an SDK message.
- `ProviderVersion.parse(String): Optional<ProviderVersion>` compares numeric components, with unknown vendor syntax remaining unknown. `ProviderVersionRange(lowerInclusive, upperExclusive).contains(version)` does not infer capability availability. The provider must inspect actual scoped evidence.
- `io.givops.adapters.testkit.AdapterTestkit.signer(String)` implements the documented length-prefixed HMAC using a synthetic key. `lease(Map<String,String>, Map<String,String>, String)` returns `MutableCredentialLease`, with `invalidate()` and `lastLocked()` for freshness tests. `verifyDefinition(ProviderDefinition)` checks descriptor basics and API compatibility.

An absent adapter makes refresh/remote writes unavailable without deleting historical observations. Duplicate provider/domain registrations or artifact IDs and incompatible API levels fail host startup. Inspection is explicit and read-only, scoped to instance/project/captured credentials; ordinary reads never require a metadata call.

Credential descriptors declare at most 32 fields. Omit empty optional fields from the supplied fields map; retain nonempty values verbatim until provider validation normalizes them. The host does not echo generic credential values in metadata.

URL reference recognition is optional via `TicketProviderDefinition.issueReference(URI): Optional<IssueReference>`. The nested record contains `issueKey` and `contextPath`; `KEY_PATTERN` describes the legacy named-key form only. Numeric identities use project-qualified keys such as `owner/repository#123`. Recognition must be local and side-effect free. The host checks configured origin, context path, project scope, ambiguity and budgets. Unsupported URL syntax is ignored; textual matching recognizes named keys, scoped `#123` and qualified `owner/repository#123`; `normalizeIssueKey(token, projectKey)` routes and normalizes per bound provider project. The `issueReference(candidate, baseUrl)` overload supports installations below a context path without guessing which URL segments belong to the project.

Host-managed credential storage retains its 32 KiB encrypted envelope limit. The complete normalized payload must fit 32739 encoded bytes (four-byte field count; sorted names and values encoded with Java `DataOutputStream.writeUTF`, including two-byte lengths). The host rejects oversize before activating a replacement. A descriptor's field/character limits do not override this aggregate bound; omit optional empty values.

## Forge and shared registration

`ProviderDefinition` owns artifact metadata, API profiles, credential fields and destination normalization. A combined JAR registers one `TicketProviderDefinition` and one `ForgeProviderDefinition`, with the same provider and artifact IDs. Its single `META-INF/givops-adapter.properties` declares `domains=ticketing,forge`, `sdkApiVersion=0.5`, and the provider, adapter ID and artifact version. Each declared domain must have exactly one registered definition. SDK 0.1, 0.2, 0.3 and 0.4 binaries are rejected.

`ForgeProvider` is an open value identity. `ForgeAdapter`, `ForgeDiscoveryAdapter`, `ForgeApiProbe`, `ForgeRepositoryIdentity`, `ForgeCapabilityInspector` and `PipelineCommandProvider` are independently registered public capabilities. Their model packages are retained. `ForgeAdapter.Access` contains ephemeral credential fields and hosting; all remote requests require the supplied gate. Provider authentication header construction belongs only in the adapter. The core owns credentials, admission, freshness, snapshots and transactions. Canonical lifecycle/CI/conflict/reviewer metadata must be supplied by the adapter; the host does not infer it from vendor raw strings.

`ForgeProviderDefinition` supplies hosting modes/default, repository identity validation, path and external-ID patterns, and an optional `commitPathTemplate` containing `{sha}`. `TicketProviderDefinition.commandLabels` describes native creation inputs/resources. `createProjectKeyPattern` and `validateCreateProjectKey` describe new-project slugs independently of bound project paths. The frontend uses these descriptors without provider-name branches.

`ForgeGroupHierarchyAdapter` is an additive, optional capability (`forge.group-hierarchy`),
independent of `ForgeDiscoveryAdapter` project/group support. `supports(provider, hosting)`
declares implementation support without I/O. `subgroups(server, access, parentExternalId,
query, limit, page, lastId, gate)` returns only direct children using the existing
`RemotePage<RemoteGroup>` model; a partial page does not establish complete ancestry.
`group(server, access, externalId, gate)` reads known ancestry metadata: the host must
derive the ID from an admitted project/group or a preceding parent result in the same
server/credential context, enforce traversal depth/object budgets, reject cycles and
verify the complete path. It must never expose this method as arbitrary public-group
ID discovery. Ancestor readability is not proof of membership. Providers independently
enforce authenticated collection scope and project import eligibility; the host owns
local group mappings, inherited-access policy and transactional import. The hierarchy
capability changes no existing discovery/import DTO or required adapter method.
This combined SDK uses exact API level 0.5; the browser API's host/adapter rebuild
requirement below also applies to adapters implementing this optional capability.

`VersionObservation.nativeState`, `startDate` and `closedAt` preserve native milestone state. `releaseDate` is the native due date for milestone providers; a closed milestone must not automatically become released. `IssueObservation.labels` preserves available native metadata. The deprecated `description` component is retained only for source migration; observation adapters must supply null and hosts must discard legacy values at read/persistence boundaries. Full ticket narratives belong exclusively in transient browser content. Forge merge-request descriptions are unchanged. The external issue ID is globally unique and stable within the configured instance; adapters without native global IDs must qualify it. The project ID describes current membership, preserving Jira identity across project moves. Local issue numbers remain scoped in issueKey.

`FailureCategory` carries provider-authored access-denial, retry and unsupported semantics. `ForgeException(code,status,message,category)` and `ReleaseWriteAdapter.WriteFailure(code,uncertain,message,category)` let the private host revoke stale access or classify retry without parsing vendor error codes. In particular, an adapter decides whether a remote 404 constitutes access denial. Provider error codes remain opaque diagnostics. Existing constructors remain source and binary compatible; supply explicit categories when host HTTP status differs from the remote cause.

## Frozen read-only browser API (0.3)

These browser contracts were introduced in API 0.3 and are retained by API 0.5.
The current exact host/SDK/adapter level is 0.5; earlier API binaries are incompatible.
Old observation constructors remain source-compatible. Artifact version
0.5.0-SNAPSHOT does not imply publication.

The optional `TicketBrowseAdapter`, `TicketContentAdapter`, `TicketAssetAdapter`
and `TicketNavigationAdapter` ports declare `ticketing.browse`,
`ticketing.content`, `ticketing.assets` and `ticketing.navigation`, respectively.
All use `TicketBrowserModels.BrowserRequest`, the existing captured
`TicketProviderAdapter.Access` and `TicketRequestGate`. Each operation validates
source/space ownership, current access and request bounds; every outbound attempt
uses gate admission. An anonymous source still supplies an access lease.

`describe` advertises accepted query fields, lifecycle scopes, sorts, snapshot
consistency and content/asset availability. Unsupported filters/sorts/scopes fail;
never ignore them. `list` returns metadata-only `TicketSummary` items,
`nextContinuation`, optional exact total, revision, captured time and effective
query. Continuations are opaque provider state inside host-signed contexts; the
host binds actor, space, source/config/access revisions, query, page size and
expiry. No speculative totals, silent truncated scans or core page-local sorting.
The literal NFC key/title profile uses case-insensitive substring matching.
Filter families combine with AND; values within a family and labels use OR.
Pages are 1–100 entries, default 50 chosen by the host. Text is at most 256
characters, and provider continuation at most 4096 characters.

`TicketRef` namespaces a stable opaque external ID by instance and external
project. `displayKey` is presentation, never persistence identity. `Lifecycle`
(ACTIVE, ARCHIVED, DRAFT) is independent of native status and canonical `Outcome`;
CANCELLED is distinct from SUCCEEDED. `SourceRevision(kind,value,immutable)`
contains an opaque full revision without assuming SHA-1. Summaries and optional
`IssueObservation.sourceRevision` preserve provenance; absence cannot establish
historical content. `detail` reads any authorized ticket, including one absent
from observation storage.

For `list` and `detail`, a null `BrowserRequest.revision` resolves the current source once and returns that captured revision; a non-null revision reads that exact revision without falling back, and browser `content` and `assets` require the pinned context.

`content` returns transient Markdown/plain text, exact source revision and an
asset/link manifest. Content is bounded to 1 MiB UTF-8 and 100 combined manifest
entries. Asset IDs are opaque IDs, never URLs or filesystem paths. Asset support
requires an immutable revision and `sha256:<64 lowercase hex>` digest of raw
asset bytes, distinct from opaque repository object identity; every asset
request must name that same revision and owning ticket. `TicketAsset` exposes
ref/revision/metadata plus a closeable stream. The host enforces its configured
byte/deadline/concurrency budget while consuming and always closes the resource.
The SDK hard cap is 10 MiB per asset and 25 MiB loaded per view. No current
HEAD fallback for an unavailable revision. A remote timestamp alone does not
prove consistency. Providers lacking rich content retain navigation or hosted
provider-link fallback; capability absence does not imply a failed observation.

Full text, comments, rendered HTML and attachment bytes must never enter ticket
observations, databases, persistent indexes, logs, disk spools or shared caches.
The host uses bounded request-local buffers/streams and no-store authenticated
responses. It validates MIME/download disposition, safe external URLs, manifest
references, rendering and authorization immediately before release. The SDK DTO
is not a sanitizer. `TicketContent.toString` redacts prose for diagnostics.

`InstanceSource` adds optional `RepositorySource(repositoryId,ref,root,
configurationPath,approvedAssetRoots,settings)`, retaining the hosted five-argument
constructor. Repository sources have null API base URL and source kind
REPOSITORY_SNAPSHOT. `sourceKinds` defaults to HOSTED_SERVICE; providers supporting
repositories override it and validate their bounded inert non-secret settings in
`validateRepositorySource`. Store namespace, parser/status/layout profile and
format paths remain adapter settings, never core provider switches.

`RepositoryContentHost.captureAccess` verifies the exact host catalog binding.
Its access lease provides `context`, locked `current`, and `resolve(revision,gate)`;
null resolves the configured ref once, while an immutable requested revision must
be authorized and retained. `Snapshot.entries(root,max,gate)` recursively returns
repository-relative entries completely or fails explicitly on scan limits.
`open(path,maxBytes,gate)` returns exact-size/object-identity closeable blobs;
reads remain confined to configured root, explicit configuration file and approved
asset roots. Reject escapes, symlinks and submodules. No automatic LFS/submodule
network reads, clone, hydration or working-tree access in a browser request.
The host supplies synchronization freshness and content-capable repository policy.

`normalizeReferenceCandidate` receives the complete bounded lexical token.
Its default recognizes the legacy named key only; adapters may recognize richer
whole tokens before project-specific `normalizeIssueKey` routing. Never truncate
a dotted candidate to its numeric parent or infer a provider from a key prefix.

The independent synthetic example implements all four browser capabilities with
unobserved tickets, complete pagination, metadata filtering, separate cancellation,
revision-bound Markdown/attachment and revoked-stream checks. The public testkit's
`verifyBrowserContent` checks summary/document/manifest coherence. Neither needs
private host source, provider credentials or a live service.

## API 0.5 repository scope

`ticketing.repository-scope` uses the independent `TicketRepositoryScopeAdapter`,
`TicketScopedProviderAdapter`, `TicketScopedBrowseAdapter` and
`TicketScopedReportAdapter` ports. A host selects each required port explicitly;
missing scoped support must fail closed without invoking its legacy counterpart.
Metadata fields expose stable IDs, COMPONENT/OPTION value kind, ONE/MANY
cardinality, EQUALS/CONTAINS semantics and the existing capability states.
Unavailable enumeration is distinct from a successful empty values list.

`TicketScopeModels.Scope` captures the module binding ID, validated field,
selected stable value ID, exact version prefix and policy/binding revisions.
Discovery and activation validation are configuration operations; runtime reads
must enforce the saved validated scope and returned membership without requiring
Jira administration discovery. Each scoped observation call has exactly one
project. Scoped ordinary calls have exactly one `OrdinaryProject`, whose
`projectId` equals `Scope.bindingId`; result envelopes echo scope and attribution.
OrdinaryProject's original four-argument constructor remains unshared.

VersionObservation and OrdinaryVersion keep `name` as the native provider name
and add `canonicalName`. Original constructors default canonicalName to name.
A scoped adapter filters by the exact case-sensitive prefix, requires a nonempty
suffix, and supplies the suffix as canonicalName while retaining native ID/name.
`ProjectQuery.releaseVersionName` is a canonical input. Prefix mapping happens
before version-selection callbacks. Form.providerVersionName previews the exact
native command target; its original constructor retains unshared behavior.
No issue bodies or arbitrary provider JSON belong in scope evidence or storage.

Command receipts add nullable `versionName` (canonical local name) and
`providerVersionName` (exact native target captured at admission). The original
Command constructor remains available and leaves both names null for legacy
receipts; hosts must not reconstruct old targets using current binding policy.
## API 0.4 provisioning

`ForgeProvisioningAdapter` is optional. Implementations create empty repositories with explicit owner/namespace selection and return authoritative IDs and canonical URLs. Exact lookup returns absence without creating, adopting, or modifying resources. Namespace creation is explicitly requested. Providers must reject unsupported default branch or visibility settings before writes. A caller must verify run ownership before resuming an existing resource. Installed definitions advertise `forge.repository-create`; runtime capability inspection retains credential/profile limitations.

Ticket project commands add an optional `projectTemplateKey`. Issue commands add typed `versionSelectors` (`NAME` or `EXTERNAL_ID`); legacy `fixVersions` semantics remain provider-defined and their constructor remains available. Mixing the two forms is invalid. OPEN/DONE commands describe the resulting observed state; adapters may create then transition when required. Unsupported rich fields fail explicitly. These provisioning contracts were introduced in API 0.4 and are retained in API 0.5. Current adapter definitions/manifests must declare 0.5.

Typed issue `VersionSelector.NAME` values are native provider names;
`EXTERNAL_ID` values are opaque provider IDs. For a shared binding, the host maps
a canonical NAME through its captured exact prefix before calling the generic
write port. IDs must never be prefixed, and a native name must not be prefixed
again. The read-only scoped ports do not alter generic issue command semantics.

## Optional ticket project lookup

A provider advertising `ticketing.project-lookup` implements `TicketProjectLookupAdapter` on its ticket read adapter. The host supplies the selected instance, the project routing key, its captured read access and a request gate. The adapter returns only the provider's external project ID, matching routing key and display name. The host rechecks credential freshness before returning the suggestion. This operation reads an existing project and makes no registration or remote mutation. Providers without this capability continue to support manual registration.

## Instance-scoped HTTPS and connection diagnostics

Forge and ticket request gates may identify the saved connection through
`connectionTarget()`. An adapter that wraps a gate forwards this target.
Hosts can provide `ConnectionClients` to select an HTTPS client for that
instance and request destination. Unregistered targets and hosts without the
extension use the adapter's ordinary client. The host owns any custom clients.
Adapter origin checks, redirect handling, timeouts and request budgets still apply.

`ConnectionFailure` supplies bounded diagnostic text for network failures and
HTTP status and media type. Forge checks carry this text in the existing
operation evidence field. Credentials, provider bodies and arbitrary headers
are excluded from this evidence.
