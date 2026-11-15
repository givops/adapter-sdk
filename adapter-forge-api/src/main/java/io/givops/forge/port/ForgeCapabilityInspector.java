package io.givops.forge.port;

import io.givops.adapters.CapabilityInspection;
import io.givops.catalog.ForgeProvider;
import io.givops.forge.model.ForgeModels.Source;

/** Explicit inspection is scoped to the instance, optional repository and captured credentials. */
public interface ForgeCapabilityInspector {
    ForgeProvider provider();
    CapabilityInspection inspect(Source source, String capability, ForgeAdapter.Access access, ForgeRequestGate gate);
}
