package io.givops.ticketing.port;

import io.givops.ticketing.model.TicketModels.InstanceSource;

/** Optional read-only lookup of an existing ticket project before local registration. */
public interface TicketProjectLookupAdapter extends TicketProviderAdapter {
    String CAPABILITY = "ticketing.project-lookup";

    record ProjectIdentity(String externalProjectId, String projectKey, String displayName) {}

    /** Resolve a routing key using the captured read credentials and host request budget. */
    ProjectIdentity lookupProject(InstanceSource source, String projectKey, Access access, TicketRequestGate gate);
}
