package io.givops.adapters;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Shared artifact, destination and credential contract for independently registered domains. */
public interface ProviderDefinition {
    String providerId();
    String displayName();
    String adapterId();
    String adapterVersion();
    default String sdkApiVersion() { return SdkCompatibility.API_VERSION; }
    List<String> capabilities();
    List<String> apiProfiles();
    List<CredentialField> credentialFields();
    URI normalizeBaseUrl(URI value);
    Map<String, String> validateCredentials(CredentialRequest target, Map<String, String> fields);
    default Optional<RuntimeCredential> runtimeCredentials(CredentialRequest target) { return Optional.empty(); }
}
