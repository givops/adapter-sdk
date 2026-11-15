package io.givops.forge.port;

import io.givops.catalog.ForgeProvider;
import java.net.URI;

/** Optional anonymous repository identity lookup; the host enforces destination and resource budgets. */
public interface ForgeRepositoryIdentity {
    ForgeProvider provider();
    Identity lookup(URI baseUrl, String repositoryPath, ForgeRequestGate gate);
    record Identity(String externalId, String repositoryPath) {}
}
