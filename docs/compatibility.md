# Compatibility and scoped capabilities

The SDK API level is `0.4`. During experimental `0.x` releases, the host requires an exact API-level match; `0.1`, `0.2` and `0.3` are incompatible even though its major is also zero. The SDK artifact version (`0.4.0-SNAPSHOT`) and adapter artifact version are independent release identifiers.

A provider product version describes a remote installation, while an API profile identifies the dialect implemented by an adapter. Neither is an SDK version. `ProviderVersion` compares parsed numeric components (`17.10` is newer than `17.9`); unrecognized vendor syntax remains unknown until the adapter normalizes it. `ProviderVersionRange` is a lower-inclusive, upper-exclusive comparison helper, not evidence that a feature exists.

Inspection is optional, explicit, read-only and scoped to the selected instance/project/captured credential context. Its states are `AVAILABLE`, `UNSUPPORTED`, `UNKNOWN`, `DISABLED`, `FORBIDDEN` and `TEMPORARILY_UNAVAILABLE`. Missing product metadata or an absent inspector means unknown; permission denial and transient failure must not be represented as unsupported. The host must revalidate the inspected context before returning a result. Existing reads do not require an extra metadata request.

The example tests simulate product versions 17 and 19, unknown versions and every outcome. These are synthetic cases, not compatibility claims about GitLab, Jira, YouTrack or another real product.


## The 0.2 boundary

Version 0.2 adds forge contracts, multi-domain registration, generic adapter
credentials and repository-scoped numeric issue references. It is an explicit
contract change from 0.1: rebuild the SDK host and installed provider libraries
together for that transition. The host rejects 0.1 libraries rather than loading
an incompatible binary. Environments using the pre-production application can be
reset and configured afresh; preserving their previous credential format is not
a requirement of this transition.

After that transition, ordinary provider URL, payload, authentication-construction
or pagination fixes that fit the 0.2 contract can be delivered as adapter-only JAR
updates followed by a restart. Adding a new host operation or changing a public
contract can still require a new SDK/core version. A public CI build and a
contract-test result do not prove every remote edition/version; each adapter must
list its tested profiles and feature limitations.

GitLab product versions such as 17 and 19, GitHub Enterprise product versions and
GitHub API date profiles are provider concerns. An adapter should use documented
feature boundaries and scoped evidence, including permissions and enabled features,
rather than equating a product major with every available capability. Unknown
metadata must remain unknown. Do not retry a mutation blindly after an ambiguous
response or interpret access denial as proof that the feature does not exist.

## The 0.3 boundary

Version 0.3 adds optional read-only browser/content/asset/navigation capabilities,
repository source configuration and a host repository-content port. It preserves
hosted source constructors and existing observation constructors for source rebuilds,
adds nullable observation source provenance, and deprecates ticket descriptions
inside observations. Rebuild all installed providers with the host and SDK at the
exact 0.3 API level. Forge merge-request descriptions and existing write operations
retain their behavior. Existing providers may remain observation/navigation-only;
new optional ports do not require vendor feature parity. See the frozen signatures
and operational rules in [CONTRACT.md](../CONTRACT.md).

## The 0.4 boundary

Version 0.4 retains the browser ports and adds optional empty-repository provisioning, exact repository lookup and post-publication default branch selection. Ticket commands add optional project templates and explicit named/ID version selectors while retaining legacy constructors and `fixVersions` behavior. Hosts, adapters, descriptors and artifact inventories must all declare API 0.4. These sources require a coordinated rebuild; earlier experimental API levels fail closed.
