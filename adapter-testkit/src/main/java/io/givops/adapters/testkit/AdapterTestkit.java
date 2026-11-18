package io.givops.adapters.testkit;

import io.givops.adapters.ContextSigner;
import io.givops.adapters.CredentialAccessException;
import io.givops.adapters.CredentialLease;
import io.givops.adapters.SdkCompatibility;
import io.givops.adapters.ProviderDefinition;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Synthetic fixtures for adapter tests; never use fixture keys or leases in production. */
public final class AdapterTestkit {
    private AdapterTestkit() { }

    /** HMAC of the namespace followed by each UTF-8 part, each prefixed by its four-byte length.
     * Provider selects the host key; it is not an extra signed part in this fixture.
     */
    public static ContextSigner signer(String syntheticSecret) {
        byte[] key = syntheticSecret.getBytes(StandardCharsets.UTF_8);
        return (provider, namespace, parts) -> {
            try {
                var mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(key, "HmacSHA256"));
                update(mac, namespace);
                parts.forEach(part -> update(mac, part));
                return HexFormat.of().formatHex(mac.doFinal());
            } catch (GeneralSecurityException failure) {
                throw new IllegalStateException("Synthetic signer is unavailable.", failure);
            }
        };
    }

    private static void update(Mac mac, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        mac.update(ByteBuffer.allocate(Integer.BYTES).putInt(bytes.length).array());
        mac.update(bytes);
    }

    public static MutableCredentialLease lease(Map<String, String> fields, Map<String, String> destinations,
                                                String accessRevision) {
        return new MutableCredentialLease(fields, destinations, accessRevision);
    }

    public static void verifyDefinition(ProviderDefinition definition) {
        SdkCompatibility.requireCompatible(definition.sdkApiVersion());
        if (definition.providerId() == null || definition.displayName() == null || definition.displayName().isBlank()
                || definition.adapterId() == null || definition.adapterId().isBlank()
                || definition.adapterVersion() == null || definition.adapterVersion().isBlank()
                || definition.capabilities() == null || definition.apiProfiles() == null
                || definition.credentialFields() == null) throw new AssertionError("Incomplete provider descriptor.");
        if (definition.capabilities().stream().distinct().count() != definition.capabilities().size()
                || definition.credentialFields().stream().map(field -> field.name()).distinct().count()
                != definition.credentialFields().size()) throw new AssertionError("Duplicate provider descriptor entries.");
    }

    /** Checks that a scoped provider preserved the caller's binding attribution and exact prefix mapping. */
    public static void verifyScopedSnapshot(io.givops.ticketing.model.TicketScopeModels.Scope expected,
            io.givops.ticketing.model.TicketScopeModels.ScopedSnapshot result) {
        if (!expected.equals(result.scope()) || result.snapshot().projects().size() != 1)
            throw new AssertionError("Scoped snapshot lost its binding attribution.");
        for (var version : result.snapshot().projects().getFirst().versions()) {
            if (!java.util.Objects.equals(expected.canonicalName(version.name()), version.canonicalName())
                    || version.canonicalName() == null) throw new AssertionError("Scoped version mapping is invalid.");
        }
    }

    /** Checks revision/content/manifest coherence without retaining text or reading asset bytes. */
    public static void verifyBrowserContent(io.givops.ticketing.model.TicketBrowserModels.TicketContent content,
                                            io.givops.ticketing.model.TicketBrowserModels.TicketSummary summary) {
        if (!content.ref().equals(summary.ref()) || !content.revision().equals(summary.revision()))
            throw new AssertionError("Browser document does not match its summary revision.");
        var assets = content.assets().stream().map(io.givops.ticketing.model.TicketBrowserModels.AssetRef::id).collect(java.util.stream.Collectors.toSet());
        for (var asset : content.assets()) {
            if (asset.id() == null || asset.id().isBlank() || asset.digest() == null || asset.digest().isBlank()
                    || asset.size() > 10 * 1024 * 1024) throw new AssertionError("Incomplete or oversized manifest asset.");
        }
        for (var link : content.links()) {
            if (link.assetId() != null && !assets.contains(link.assetId())) throw new AssertionError("Manifest link targets an undeclared asset.");
        }
    }

    public static final class MutableCredentialLease implements CredentialLease {
        private final Map<String, String> fields;
        private final Map<String, String> destinations;
        private final String accessRevision;
        private final AtomicBoolean current = new AtomicBoolean(true);
        private volatile boolean lastLocked;

        private MutableCredentialLease(Map<String, String> fields, Map<String, String> destinations, String revision) {
            this.fields = Map.copyOf(fields);
            this.destinations = Map.copyOf(destinations);
            this.accessRevision = revision;
        }
        @Override public Map<String, String> fields() { return fields; }
        @Override public Map<String, String> destinations() { return destinations; }
        @Override public String accessRevision() { return accessRevision; }
        @Override public String mode() { return "MANAGED"; }
        @Override public boolean legacyRuntime() { return false; }
        @Override public void requireCurrent(boolean locked) {
            lastLocked = locked;
            if (!current.get()) throw new CredentialAccessException();
        }
        public void invalidate() { current.set(false); }
        public boolean lastLocked() { return lastLocked; }
        @Override public String toString() { return "MutableCredentialLease[redacted]"; }
    }
}
