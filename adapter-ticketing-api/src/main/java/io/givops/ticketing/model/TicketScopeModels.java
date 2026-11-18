package io.givops.ticketing.model;

import io.givops.adapters.CapabilityInspection.State;
import io.givops.ticketing.model.TicketModels.*;
import io.givops.ticketing.port.TicketProviderAdapter.VersionMemberships;
import io.givops.testreports.model.TestReportProviderModels.OrdinaryDataset;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Validated, provider-neutral binding scope. Contains no issue bodies or provider JSON. */
public final class TicketScopeModels {
    private TicketScopeModels() { }
    public static final String CAPABILITY = "ticketing.repository-scope";
    public enum ValueKind { COMPONENT, OPTION }
    public enum Cardinality { ONE, MANY }
    public enum Match { EQUALS, CONTAINS }
    public record Field(String id, String label, ValueKind valueKind, Cardinality cardinality,
                        Match match, State state, String diagnostic) { }
    public record Value(String id, String label, boolean disabled, State state, String diagnostic) { }
    public record Fields(State state, List<Field> fields, String diagnostic) {
        public Fields { fields = List.copyOf(fields); }
    }
    public record Values(State state, List<Value> values, String diagnostic) {
        public Values { values = List.copyOf(values); }
    }
    /** bindingId is the host module/project ID, independent of the ticket-project registration ID. */
    public record Scope(UUID bindingId, Field field, String valueId, String versionPrefix,
                        long policyRevision, long bindingRevision) {
        public Scope {
            Objects.requireNonNull(bindingId); Objects.requireNonNull(field);
            if (field.id() == null || field.id().isBlank() || field.state() != State.AVAILABLE
                    || field.valueKind() == null || field.cardinality() == null || field.match() == null
                    || field.cardinality() == Cardinality.ONE && field.match() != Match.EQUALS
                    || field.cardinality() == Cardinality.MANY && field.match() != Match.CONTAINS
                    || valueId == null || valueId.isBlank() || valueId.length() > 256
                    || valueId.codePoints().anyMatch(Character::isISOControl)
                    || versionPrefix == null || versionPrefix.isBlank() || versionPrefix.length() > 256
                    || versionPrefix.codePoints().anyMatch(Character::isISOControl)
                    || policyRevision < 0 || bindingRevision < 0) throw new IllegalArgumentException("Invalid repository scope.");
        }
        /** Exact, case-sensitive prefix. A bare prefix has no eligible local version. */
        public String canonicalName(String providerName) {
            return providerName != null && providerName.startsWith(versionPrefix) && providerName.length() > versionPrefix.length()
                    ? providerName.substring(versionPrefix.length()) : null;
        }
        public String providerName(String canonicalName) {
            if (canonicalName == null || canonicalName.isEmpty()) throw new IllegalArgumentException("Missing local version name.");
            return versionPrefix + canonicalName;
        }
    }
    public record ScopedSnapshot(Scope scope, ProviderSnapshot snapshot) {
        public ScopedSnapshot { Objects.requireNonNull(scope); Objects.requireNonNull(snapshot); }
    }
    public record ScopedMemberships(Scope scope, VersionMemberships memberships) {
        public ScopedMemberships { Objects.requireNonNull(scope); Objects.requireNonNull(memberships); }
    }
    public record ScopedOrdinaryDataset(Scope scope, UUID projectId, OrdinaryDataset dataset) {
        public ScopedOrdinaryDataset { Objects.requireNonNull(scope); Objects.requireNonNull(projectId); Objects.requireNonNull(dataset); }
    }
}
