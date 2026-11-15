package io.givops.ticketing.port;
import io.givops.adapters.CapabilityInspection;
import io.givops.ticketing.model.TicketModels.InstanceSource;
import io.givops.ticketing.model.TicketModels.TicketProvider;
/** Optional explicit read-only inspection. Ordinary reads never require a metadata request. */
public interface TicketCapabilityInspector {
    TicketProvider provider();
    CapabilityInspection inspect(InstanceSource source, String externalProjectId, String capability,
                                 TicketProviderAdapter.Access access, TicketRequestGate gate);
}
