package io.givops.ticketing.port;
import io.givops.adapters.CredentialField;
import io.givops.adapters.CredentialRequest;
import io.givops.adapters.RuntimeCredential;
import io.givops.ticketing.model.TicketModels.TicketProvider;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
/** Public configuration and credential contract registered by an installed library. */
public interface TicketProviderDefinition extends io.givops.adapters.ProviderDefinition {
    @Override default String providerId() { return provider().name(); }
    TicketProvider provider();
    String displayName();
    String adapterId();
    String adapterVersion();
    default String sdkApiVersion() { return io.givops.adapters.SdkCompatibility.API_VERSION; }
    List<String> capabilities();
    List<String> apiProfiles();
    List<CredentialField> credentialFields();
    default Map<String, String> commandLabels() { return Map.of(); }
    default String projectKeyPattern() { return null; }
    default String createProjectKeyPattern() { return projectKeyPattern(); }
    default void validateCreateProjectKey(String projectKey) {
        if (projectKey == null || projectKey.isBlank() || projectKey.length() > 512
                || createProjectKeyPattern() != null && !projectKey.matches(createProjectKeyPattern()))
            throw new io.givops.ticketing.model.TicketException(io.givops.ticketing.model.TicketException.Code.INVALID_CONFIGURATION,
                    io.givops.adapters.i18n.SdkMessages.format("sdk.provider.project.key.invalid"));
    }
    default String externalIdPattern() { return null; }
    /** Optional URL syntax recognition only. The host retains origin, context, routing and budget checks. */
    default Optional<IssueReference> issueReference(URI candidate) { return Optional.empty(); }
    default Optional<IssueReference> issueReference(URI candidate, URI baseUrl) { return issueReference(candidate); }
    /** Normalize a candidate in one bound project, or return empty when it belongs elsewhere. */
    default Optional<String> normalizeIssueKey(String token, String projectKey) {
        if (token == null || !token.matches(IssueReference.KEY_PATTERN)) return Optional.empty();
        String canonical = token.toUpperCase(java.util.Locale.ROOT);
        return canonical.substring(0, canonical.lastIndexOf('-')).equals(projectKey)
                ? Optional.of(canonical) : Optional.empty();
    }
    record IssueReference(String issueKey, String contextPath) {
        public static final String KEY_PATTERN = "[A-Za-z][A-Za-z0-9_]{1,63}-[1-9][0-9]{0,18}";
        public IssueReference {
            if (issueKey == null || issueKey.isBlank() || issueKey.length() > 1024
                    || issueKey.codePoints().anyMatch(Character::isISOControl) || contextPath == null
                    || !contextPath.matches("(?:/[A-Za-z0-9._-]+)*"))
                throw new IllegalArgumentException(io.givops.adapters.i18n.SdkMessages.format("sdk.reference.invalid"));
        }
    }
    URI normalizeBaseUrl(URI value);
    void validateProject(String externalId, String projectKey);
    default void validateProjectKey(String projectKey) {
        if (projectKey == null || projectKey.isBlank() || projectKey.length() > 512
                || projectKeyPattern() != null && !projectKey.matches(projectKeyPattern()))
            throw new io.givops.ticketing.model.TicketException(io.givops.ticketing.model.TicketException.Code.INVALID_CONFIGURATION,
                    io.givops.adapters.i18n.SdkMessages.format("sdk.provider.project.key.invalid"));
    }
    Map<String, String> validateCredentials(CredentialRequest target, Map<String, String> fields);
    default Optional<RuntimeCredential> runtimeCredentials(CredentialRequest target) { return Optional.empty(); }
}
