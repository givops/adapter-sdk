package io.givops.forge.port;

import io.givops.adapters.ProviderDefinition;
import io.givops.catalog.ForgeProvider;
import io.givops.forge.model.ForgeConnectionSettings.Hosting;
import java.util.List;

/** Public forge setup and credential descriptor, supplied by the installed adapter. */
public interface ForgeProviderDefinition extends ProviderDefinition {
    ForgeProvider provider();
    @Override default String providerId() { return provider().name(); }
    default Hosting defaultHosting() { return Hosting.UNKNOWN; }
    default List<Hosting> hostingModes() { return List.of(Hosting.UNKNOWN); }
    default String repositoryPathPattern() { return null; }
    default String externalIdPattern() { return null; }
    default String commitPathTemplate() { return null; }
    void validateRepository(String externalId, String repositoryPath);
}
