package io.givops.adapters;
import java.time.Instant;
import java.util.List;
/** Observations are scoped to the captured connection/project/credential context, never inferred globally. */
public record CapabilityInspection(String provider, String capability, State state, String reason,
                                   String productVersion, String apiProfile, String context, Instant observedAt) {
    public enum State { AVAILABLE, UNSUPPORTED, UNKNOWN, DISABLED, FORBIDDEN, TEMPORARILY_UNAVAILABLE }
}
