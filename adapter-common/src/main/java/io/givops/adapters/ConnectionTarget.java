package io.givops.adapters;

import java.util.Objects;
import java.util.UUID;

/** Identifies the registered instance on whose behalf a provider is making a request. */
public record ConnectionTarget(Kind kind, UUID id) {
    public enum Kind { FORGE, TICKETING }

    public ConnectionTarget {
        Objects.requireNonNull(kind);
        Objects.requireNonNull(id);
    }

    public static ConnectionTarget forge(UUID id) { return new ConnectionTarget(Kind.FORGE, id); }
    public static ConnectionTarget ticketing(UUID id) { return new ConnectionTarget(Kind.TICKETING, id); }

    public interface Provider {
        /** Unregistered previews and hosts without per-instance TLS use the ordinary system client. */
        default ConnectionTarget connectionTarget() { return null; }
    }
}
