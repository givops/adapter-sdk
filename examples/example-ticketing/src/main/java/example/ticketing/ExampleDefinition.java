package example.ticketing;

import io.givops.adapters.CredentialField;
import io.givops.adapters.CredentialRequest;
import io.givops.ticketing.model.TicketException;
import io.givops.ticketing.model.TicketModels.TicketProvider;
import io.givops.ticketing.port.TicketProviderDefinition;
import java.net.URI;
import java.util.List;
import java.util.Map;

/** Synthetic third provider: no private host classes or vendor protocol assumptions. */
public final class ExampleDefinition implements TicketProviderDefinition {
    public static final TicketProvider PROVIDER = TicketProvider.valueOf("EXAMPLE_TICKETS");
    @Override public TicketProvider provider() { return PROVIDER; }
    @Override public String displayName() { return ExampleMessages.format("example.name"); }
    @Override public String adapterId() { return "io.givops.example.ticketing"; }
    @Override public String adapterVersion() { return "0.2.0-SNAPSHOT"; }
    @Override public List<String> capabilities() { return List.of("ticketing.read"); }
    @Override public List<String> apiProfiles() { return List.of("example-rest-v1"); }
    @Override public List<CredentialField> credentialFields() {
        return List.of(new CredentialField("apiToken", ExampleMessages.format("example.token"), CredentialField.Type.SECRET, true, 4096));
    }
    @Override public String projectKeyPattern() { return "[A-Z][A-Z0-9_]{1,63}"; }
    @Override public String externalIdPattern() { return "[A-Za-z0-9_-]{1,128}"; }
    @Override public java.util.Optional<IssueReference> issueReference(URI candidate) {
        if (candidate.getRawPath() == null) return java.util.Optional.empty();
        var match = java.util.regex.Pattern.compile("/tickets/(" + IssueReference.KEY_PATTERN + ")").matcher(candidate.getRawPath());
        return match.matches() ? java.util.Optional.of(new IssueReference(match.group(1).toUpperCase(java.util.Locale.ROOT), "")) : java.util.Optional.empty();
    }
    @Override public URI normalizeBaseUrl(URI value) {
        if (value == null || !"https".equalsIgnoreCase(value.getScheme()) || value.getHost() == null
                || value.getUserInfo() != null || value.getQuery() != null || value.getFragment() != null
                || value.getPort() != -1 && value.getPort() != 443 || !List.of("", "/").contains(value.getPath())) throw invalid();
        return URI.create("https://" + value.getHost().toLowerCase(java.util.Locale.ROOT));
    }
    @Override public void validateProject(String externalId, String projectKey) {
        validateProjectKey(projectKey);
        if (externalId == null || !externalId.matches(externalIdPattern())) throw invalid();
    }
    @Override public Map<String, String> validateCredentials(CredentialRequest target, Map<String, String> fields) {
        if (!fields.keySet().equals(java.util.Set.of("apiToken")) || fields.get("apiToken").isBlank()
                || fields.get("apiToken").length() > 4096 || fields.get("apiToken").codePoints().anyMatch(Character::isISOControl)) throw invalid();
        return Map.copyOf(fields);
    }
    private static TicketException invalid() {
        return new TicketException(TicketException.Code.INVALID_CONFIGURATION, ExampleMessages.format("example.invalid"));
    }
}
