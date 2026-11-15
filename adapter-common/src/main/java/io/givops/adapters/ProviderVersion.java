package io.givops.adapters;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** Numeric provider product version; a version alone never establishes capability availability.
 * Vendor-specific suffixes must be normalized by the provider; unrecognized input remains unknown.
 */
public final class ProviderVersion implements Comparable<ProviderVersion> {
    private final List<BigInteger> parts;
    private ProviderVersion(List<BigInteger> parts) { this.parts = List.copyOf(parts); }
    public static Optional<ProviderVersion> parse(String value) {
        if (value == null || value.length() > 128 || !value.matches("[0-9]+(?:\\.[0-9]+)*")) return Optional.empty();
        var parts = new java.util.ArrayList<>(Arrays.stream(value.split("\\.")).map(BigInteger::new).toList());
        while (parts.size() > 1 && parts.getLast().signum() == 0) parts.removeLast();
        return Optional.of(new ProviderVersion(parts));
    }
    @Override public int compareTo(ProviderVersion other) {
        for (int i = 0; i < Math.max(parts.size(), other.parts.size()); i++) {
            int comparison = (i < parts.size() ? parts.get(i) : BigInteger.ZERO)
                    .compareTo(i < other.parts.size() ? other.parts.get(i) : BigInteger.ZERO);
            if (comparison != 0) return comparison;
        }
        return 0;
    }
    @Override public boolean equals(Object value) { return value instanceof ProviderVersion other && parts.equals(other.parts); }
    @Override public int hashCode() { return parts.hashCode(); }
    @Override public String toString() { return String.join(".", parts.stream().map(BigInteger::toString).toList()); }
}
