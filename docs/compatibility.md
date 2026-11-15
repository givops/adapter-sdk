# Compatibility and scoped capabilities

The SDK API level is `0.1`. During experimental `0.x` releases, the host requires an exact API-level match; `0.2` is incompatible even though its major is also zero. The SDK artifact version (`0.1.0-SNAPSHOT`) and adapter artifact version are independent release identifiers.

A provider product version describes a remote installation, while an API profile identifies the dialect implemented by an adapter. Neither is an SDK version. `ProviderVersion` compares parsed numeric components (`17.10` is newer than `17.9`); unrecognized vendor syntax remains unknown until the adapter normalizes it. `ProviderVersionRange` is a lower-inclusive, upper-exclusive comparison helper, not evidence that a feature exists.

Inspection is optional, explicit, read-only and scoped to the selected instance/project/captured credential context. Its states are `AVAILABLE`, `UNSUPPORTED`, `UNKNOWN`, `DISABLED`, `FORBIDDEN` and `TEMPORARILY_UNAVAILABLE`. Missing product metadata or an absent inspector means unknown; permission denial and transient failure must not be represented as unsupported. The host must revalidate the inspected context before returning a result. Existing reads do not require an extra metadata request.

The example tests simulate product versions 17 and 19, unknown versions and every outcome. These are synthetic cases, not compatibility claims about GitLab, Jira, YouTrack or another real product.
