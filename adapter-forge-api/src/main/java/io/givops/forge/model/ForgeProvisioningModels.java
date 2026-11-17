package io.givops.forge.model;

/** Commands and authoritative remote identities for empty repository provisioning. */
public final class ForgeProvisioningModels {
    private ForgeProvisioningModels() { }

    /** Namespace paths are explicit; creation never initializes a README, license, or Git reference. */
    public record CreateRepositoryCommand(String namespacePath, String name, String description,
            String defaultBranch, boolean privateRepository, boolean createNamespaces) { }

    /** Provider-returned canonical URLs and IDs; empty means no Git references exist. */
    public record Repository(String externalId, String fullPath, String namespacePath, String name,
            String description, String cloneUrl, String webUrl, String defaultBranch,
            boolean privateRepository, boolean empty) { }
}
