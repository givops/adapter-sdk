# Compatibility and scoped capabilities

The SDK API level is `0.2`. During experimental `0.x` releases, the host requires an exact API-level match; `0.1` is incompatible even though its major is also zero. The SDK artifact version (`0.2.0-SNAPSHOT`) and adapter artifact version are independent release identifiers.

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
