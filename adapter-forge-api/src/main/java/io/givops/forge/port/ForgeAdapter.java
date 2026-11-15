package io.givops.forge.port;

import io.givops.adapters.i18n.SdkMessages;
import io.givops.catalog.ForgeProvider;
import io.givops.forge.model.ForgeConnectionSettings.Hosting;
import io.givops.forge.model.ForgeException;
import io.givops.forge.model.ForgeModels.Branch;
import io.givops.forge.model.ForgeModels.BranchPipeline;
import io.givops.forge.model.ForgeModels.RefreshInput;
import io.givops.forge.model.ForgeModels.Settings;
import io.givops.forge.model.ForgeModels.Snapshot;
import io.givops.forge.model.ForgeModels.Source;
import java.net.URI;
import org.springframework.http.HttpStatus;

public interface ForgeAdapter {
    ForgeProvider provider();

    default boolean supports(URI baseUrl) { return true; }

    /** Local adapter context only. Credential identity is captured separately by the application service. */
    default String authenticationContext(Source source) { return provider().name(); }

    Snapshot read(RefreshInput input, ForgeRequestGate gate, Access access);

    default BranchPipeline readBranchPipeline(Source source, Settings settings, Branch branch,
                                              ForgeRequestGate gate, Access access) {
        throw new ForgeException("forge_provider_unsupported", HttpStatus.UNPROCESSABLE_CONTENT,
                SdkMessages.format("forge.forgeAdapter.exact.branch.pipeline.observations.are.unavailable.for.this.provider"));
    }

    record Access(java.util.Map<String, String> fields, Hosting hosting) {
        public Access { fields = java.util.Map.copyOf(fields); }
        public Access(String token, Hosting hosting) { this(token == null ? java.util.Map.of() : java.util.Map.of("apiToken", token), hosting); }
        public String token() { return fields.get("apiToken"); }
        @Override public String toString() { return "Access[redacted]"; }
    }
}
