package io.givops.adapters;
import java.util.Map;
public record RuntimeCredential(Map<String, String> fields, Map<String, String> expectedDestinations, String accessRevision) {
    public RuntimeCredential { fields = Map.copyOf(fields); expectedDestinations = Map.copyOf(expectedDestinations); }
    @Override public String toString() { return "RuntimeCredential[redacted]"; }
}
