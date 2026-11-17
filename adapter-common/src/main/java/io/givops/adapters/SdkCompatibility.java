package io.givops.adapters;

import io.givops.adapters.i18n.SdkMessages;

/** API compatibility is separate from artifact/product versions and provider API dialects. */
public final class SdkCompatibility {
    public static final String API_VERSION = "0.4";
    private SdkCompatibility() { }
    /** Experimental 0.x API levels require an exact match; artifact patch versions are independent. */
    public static void requireCompatible(String apiVersion) {
        if (!API_VERSION.equals(apiVersion)) throw new IllegalArgumentException(SdkMessages.format("sdk.api.incompatible"));
    }
}
