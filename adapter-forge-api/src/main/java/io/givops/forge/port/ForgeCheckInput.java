package io.givops.forge.port;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

/** An approved target and ephemeral credential fields; never persist or log this object. */
public record ForgeCheckInput(Scope scope, Map<String, String> credentials, String provider,
                             URI baseUrl, String instanceKey, RepositoryTarget repository) {
    public enum Scope { FORGE_API, REPOSITORY_API }
    public record RepositoryTarget(UUID id, String path, String externalId, URI cloneUrl) {}
    public ForgeCheckInput { credentials = Map.copyOf(credentials); }
    @Override public String toString() { return "ForgeCheckInput[redacted]"; }
}
