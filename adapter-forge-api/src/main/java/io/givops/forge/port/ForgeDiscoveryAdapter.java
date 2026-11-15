package io.givops.forge.port;

import io.givops.catalog.ForgeProvider;
import io.givops.forge.discovery.DiscoveryModels.RemoteGroup;
import io.givops.forge.discovery.DiscoveryModels.RemotePage;
import io.givops.forge.discovery.DiscoveryModels.RemoteProject;
import io.givops.forge.discovery.DiscoveryModels.Server;
import io.givops.forge.model.ForgeConnectionSettings.Hosting;

/** Server-scoped discovery is independent of repository observation refresh. */
public interface ForgeDiscoveryAdapter {
    enum Operation { PROJECT_DISCOVERY, GROUP_DISCOVERY }
    record Support(boolean projectDiscovery, boolean groupDiscovery) {
        public boolean allows(Operation operation) {
            return switch (operation) {
                case PROJECT_DISCOVERY -> projectDiscovery;
                case GROUP_DISCOVERY -> groupDiscovery;
            };
        }
    }
    /** Declare each server operation independently; no remote call is needed for implementation support. */
    Support support(ForgeProvider provider, Hosting hosting);
    default boolean supports(ForgeProvider provider, Hosting hosting, Operation operation) {
        return support(provider, hosting).allows(operation);
    }
    RemotePage<RemoteGroup> groups(Server server, ForgeAdapter.Access access, String query, int limit, int page,
                                  long lastId, ForgeRequestGate gate);
    RemotePage<RemoteProject> projects(Server server, ForgeAdapter.Access access, String groupId, String query,
                                     int limit, int page, long lastId, ForgeRequestGate gate);
    RemoteProject project(Server server, ForgeAdapter.Access access, String externalId, ForgeRequestGate gate);
}
