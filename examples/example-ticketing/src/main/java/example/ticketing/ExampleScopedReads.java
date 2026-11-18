package example.ticketing;

import io.givops.ticketing.model.TicketModels.*;
import io.givops.ticketing.model.TicketScopeModels.*;
import io.givops.ticketing.model.TicketException;
import io.givops.ticketing.port.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** Synthetic optional read port. This partial example deliberately does not advertise the complete scope capability. */
public final class ExampleScopedReads implements TicketScopedProviderAdapter {
    private final TicketProviderAdapter delegate;
    public ExampleScopedReads(TicketProviderAdapter delegate) { this.delegate = delegate; }
    @Override public TicketProvider provider() { return delegate.provider(); }
    private void require(Scope scope) {
        if (scope == null || !"repository".equals(scope.field().id()) || !Set.of("back", "front").contains(scope.valueId()))
            throw new TicketException(TicketException.Code.INVALID_CONFIGURATION, "Unsupported synthetic repository scope.");
    }
    @Override public ScopedSnapshot readScoped(ProviderInput input, Scope scope, TicketRequestGate gate, TicketProviderAdapter.Access access) {
        require(scope);
        if (input.projects().size() != 1) throw new TicketException(TicketException.Code.INVALID_CONFIGURATION, "One scoped binding is required.");
        var result = delegate.read(input, gate, access).projects().getFirst();
        var versions = List.of("Back 1.0.0", "Front 1.0.0", "1.0.0").stream()
                .filter(name -> scope.canonicalName(name) != null)
                .map(name -> new VersionObservation("synthetic-" + name, name, true, false, null, input.capturedAt(),
                        null, null, null, scope.canonicalName(name))).toList();
        gate.acceptedObjects(versions.size()); gate.check();
        return new ScopedSnapshot(scope, new ProviderSnapshot(List.of(new ProjectResult(result.ticketProjectId(), result.project(),
                versions, result.issues(), result.unresolvedKeys(), result.releaseMembership(), result.assignedIssues()))));
    }
    @Override public ScopedMemberships readVersionMembershipsScoped(InstanceSource source, ProjectQuery query, Scope scope,
            TicketProviderAdapter.Access access, TicketRequestGate gate, Function<List<VersionObservation>, Set<String>> select,
            int maximumVersions, int maximumIssues) {
        var result = readScoped(new ProviderInput(source, List.of(query), java.time.Instant.EPOCH), scope, gate, access);
        var versions = result.snapshot().projects().getFirst().versions();
        var selected = Set.copyOf(select.apply(versions));
        if (selected.size() > maximumVersions || !versions.stream().map(VersionObservation::externalVersionId).toList().containsAll(selected))
            throw new TicketException(TicketException.Code.INVALID_CONFIGURATION, "Invalid synthetic version selection.");
        var memberships = new java.util.LinkedHashMap<String, List<IssueObservation>>();
        selected.forEach(id -> memberships.put(id, List.of()));
        return new ScopedMemberships(scope, new TicketProviderAdapter.VersionMemberships(memberships));
    }
}
