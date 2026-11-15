package io.givops.adapters;

import io.givops.adapters.i18n.SdkMessages;

/** Numeric lower-inclusive/upper-exclusive range. Null bounds are unbounded. */
public record ProviderVersionRange(ProviderVersion lowerInclusive, ProviderVersion upperExclusive) {
    public ProviderVersionRange {
        if (lowerInclusive != null && upperExclusive != null && lowerInclusive.compareTo(upperExclusive) >= 0)
            throw new IllegalArgumentException(SdkMessages.format("sdk.version.range.invalid"));
    }
    public boolean contains(ProviderVersion version) {
        return version != null && (lowerInclusive == null || version.compareTo(lowerInclusive) >= 0)
                && (upperExclusive == null || version.compareTo(upperExclusive) < 0);
    }
}
