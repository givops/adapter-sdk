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
public interface TicketProviderDefinition {
    TicketProvider provider();
    String displayName();
    String adapterId();
    String adapterVersion();
    default String sdkApiVersion() { return io.givops.adapters.SdkCompatibility.API_VERSION; }
    List<String> capabilities();
    List<String> apiProfiles();
    List<CredentialField> credentialFields();
    default String projectKeyPattern() { return null; }
    default String externalIdPattern() { return null; }
    URI normalizeBaseUrl(URI value);
    void validateProject(String externalId, String projectKey);
    default void validateProjectKey(String projectKey) {
        if (projectKey == null || projectKey.isBlank() || projectKey.length() > 128
                || projectKeyPattern() != null && !projectKey.matches(projectKeyPattern()))
            throw new io.givops.ticketing.model.TicketException(io.givops.ticketing.model.TicketException.Code.INVALID_CONFIGURATION,
                    io.givops.adapters.i18n.SdkMessages.format("sdk.provider.project.key.invalid"));
    }
    Map<String, String> validateCredentials(CredentialRequest target, Map<String, String> fields);
    default Optional<RuntimeCredential> runtimeCredentials(CredentialRequest target) { return Optional.empty(); }
}
