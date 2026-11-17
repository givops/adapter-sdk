package io.givops.ticketing.port;

import io.givops.ticketing.model.TicketModels.*;
import io.givops.ticketing.model.TicketScopeModels.*;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/** Explicit scoped operations. Hosts must never fall back to an unscoped adapter. One binding per call. */
public interface TicketScopedProviderAdapter {
    TicketProvider provider();
    ScopedSnapshot readScoped(ProviderInput input, Scope scope, TicketRequestGate gate, TicketProviderAdapter.Access access);
    ScopedMemberships readVersionMembershipsScoped(InstanceSource source, ProjectQuery query, Scope scope,
            TicketProviderAdapter.Access access, TicketRequestGate gate,
            Function<List<VersionObservation>, Set<String>> select, int maximumVersions, int maximumIssues);
}
