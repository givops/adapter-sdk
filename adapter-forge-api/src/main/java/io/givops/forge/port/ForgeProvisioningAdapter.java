package io.givops.forge.port;

import io.givops.catalog.ForgeProvider;
import io.givops.forge.discovery.DiscoveryModels.Server;
import io.givops.forge.model.ForgeConnectionSettings.Hosting;
import io.givops.forge.model.ForgeProvisioningModels.CreateRepositoryCommand;
import io.givops.forge.model.ForgeProvisioningModels.Repository;
import java.util.Optional;

/** Optional server-scoped provisioning; lookup never adopts or modifies an existing resource. */
public interface ForgeProvisioningAdapter {
    String CAPABILITY = "forge.repository-create";
    boolean supports(ForgeProvider provider, Hosting hosting);
    Repository createRepository(Server server, CreateRepositoryCommand command,
            ForgeAdapter.Access access, ForgeRequestGate gate);
    /** Set only an existing pushed branch; implementation verifies the observed resulting default. */
    default void setDefaultBranch(Server server, String externalId, String fullPath, String branch,
            ForgeAdapter.Access access, ForgeRequestGate gate) {
        throw new io.givops.forge.model.ForgeException("forge_default_branch_unsupported",
                org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT, io.givops.adapters.i18n.SdkMessages.format("forge.provisioning.default-branch.unsupported"));
    }
    Optional<Repository> findRepository(Server server, String namespacePath, String name,
            ForgeAdapter.Access access, ForgeRequestGate gate);
}
