package io.givops.forge.config;

import io.givops.adapters.i18n.SdkMessages;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("givops.forge")
public record ForgeProperties(
        @DefaultValue("120s") Duration refreshTimeout,
        @DefaultValue("10s") Duration requestTimeout,
        @DefaultValue("300") int maxRequests,
        @DefaultValue("20") int maxPages,
        @DefaultValue("2097152") long maxResponseBytes,
        @DefaultValue("16777216") long maxTotalBytes,
        @DefaultValue("500") int maxMergeRequests,
        @DefaultValue("200") int maxBranches,
        @DefaultValue("5000") int maxAssociations,
        @DefaultValue("10000") int maxObjects,
        @DefaultValue("4") int maxConcurrentPerForge,
        @DefaultValue("5m") Duration associationTtl,
        @DefaultValue("15m") Duration freshness,
        @DefaultValue("50") int retainedAttempts) {
    public ForgeProperties {
        if (refreshTimeout == null || refreshTimeout.isNegative() || refreshTimeout.isZero()
                || refreshTimeout.compareTo(Duration.ofMinutes(10)) > 0
                || requestTimeout == null || requestTimeout.isNegative() || requestTimeout.isZero()
                || requestTimeout.compareTo(Duration.ofSeconds(60)) > 0
                || associationTtl == null || associationTtl.isNegative() || associationTtl.isZero()
                || associationTtl.compareTo(Duration.ofHours(1)) > 0
                || freshness == null || freshness.isNegative() || freshness.isZero()
                || freshness.compareTo(Duration.ofHours(24)) > 0
                || maxRequests < 1 || maxRequests > 1000 || maxPages < 1 || maxPages > 100
                || maxResponseBytes < 1 || maxResponseBytes > 8_388_608
                || maxTotalBytes < 1 || maxTotalBytes > 67_108_864
                || maxMergeRequests < 1 || maxMergeRequests > 2000 || maxBranches < 1 || maxBranches > 1000
                || maxAssociations < 1 || maxAssociations > 20000 || maxObjects < 1 || maxObjects > 40000
                || maxConcurrentPerForge < 1 || maxConcurrentPerForge > 16
                || retainedAttempts < 1 || retainedAttempts > 500) {
            throw new IllegalArgumentException(SdkMessages.format("forge.forgeProperties.invalid.forge.resource.limits"));
        }
    }
}
