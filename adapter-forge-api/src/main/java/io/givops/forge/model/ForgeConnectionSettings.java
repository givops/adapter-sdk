package io.givops.forge.model;

import java.time.Instant;

public record ForgeConnectionSettings(long revision, boolean apiEnabled, Hosting hosting, Instant updatedAt) {
    public enum Hosting { SAAS, SELF_MANAGED, DEDICATED, UNKNOWN }
}
