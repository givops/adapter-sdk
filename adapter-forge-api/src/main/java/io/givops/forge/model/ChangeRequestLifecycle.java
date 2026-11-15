package io.givops.forge.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/** Canonical lifecycle supplied by the provider adapter, independent of remote state spelling. */
public enum ChangeRequestLifecycle {
    OPEN, MERGED, CLOSED, UNKNOWN;

    @JsonCreator
    public static ChangeRequestLifecycle fromJson(String value) {
        if (value == null) return null;
        try { return valueOf(value); } catch (IllegalArgumentException unknown) { return UNKNOWN; }
    }
}
