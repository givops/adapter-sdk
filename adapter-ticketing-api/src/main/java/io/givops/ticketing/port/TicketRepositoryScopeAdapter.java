package io.givops.ticketing.port;

import io.givops.adapters.CapabilityInspection;
import io.givops.ticketing.model.TicketModels.*;
import io.givops.ticketing.model.TicketScopeModels.*;

/** Optional metadata capability. Runtime reads use positively validated configuration without admin discovery. */
public interface TicketRepositoryScopeAdapter {
    TicketProvider provider();
    Fields fields(InstanceSource source, String externalProjectId, TicketProviderAdapter.Access access, TicketRequestGate gate);
    Values values(InstanceSource source, String externalProjectId, String fieldId, TicketProviderAdapter.Access access, TicketRequestGate gate);
    CapabilityInspection validateScope(InstanceSource source, String externalProjectId, Scope scope,
                                       TicketProviderAdapter.Access access, TicketRequestGate gate);
}
