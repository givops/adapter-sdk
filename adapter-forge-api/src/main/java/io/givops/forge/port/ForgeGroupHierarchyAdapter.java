package io.givops.forge.port;

import io.givops.catalog.ForgeProvider;
import io.givops.forge.discovery.DiscoveryModels.RemoteGroup;
import io.givops.forge.discovery.DiscoveryModels.RemotePage;
import io.givops.forge.discovery.DiscoveryModels.Server;
import io.givops.forge.model.ForgeConnectionSettings.Hosting;

/** Optional, read-only hierarchy capability, independent of project and group discovery. */
public interface ForgeGroupHierarchyAdapter {
    boolean supports(ForgeProvider provider, Hosting hosting);

    /**
     * Read metadata for a known namespace or parent, not arbitrary group discovery.
     * The host must derive the ID from an admitted project/group or preceding parent result
     * in the same server and credential context, bound traversal and reject cycles.
     * A readable ancestor does not establish membership or authorize project import.
     */
    RemoteGroup group(Server server, ForgeAdapter.Access access, String externalId, ForgeRequestGate gate);

    /** List direct children only; remote continuation must retain the supplied scope and filters. */
    RemotePage<RemoteGroup> subgroups(Server server, ForgeAdapter.Access access, String parentExternalId,
                                    String query, int limit, int page, long lastId, ForgeRequestGate gate);
}
