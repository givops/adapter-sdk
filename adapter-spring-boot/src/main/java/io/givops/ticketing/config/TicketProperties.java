package io.givops.ticketing.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties("givops.ticketing")
public record TicketProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("120s") @NotNull Duration refreshTimeout,
        @DefaultValue("10s") @NotNull Duration requestTimeout,
        @DefaultValue("100") @Min(1) @Max(1000) int maxHttpAttempts,
        @DefaultValue("20") @Min(1) @Max(100) int maxPages,
        @DefaultValue("2097152") @Min(1) @Max(16777216) long maxResponseBytes,
        @DefaultValue("16777216") @Min(1) @Max(134217728) long maxTotalResponseBytes,
        @DefaultValue("500") @Min(1) @Max(5000) int maxIssueKeys,
        @DefaultValue("500") @Min(1) @Max(5000) int maxReleaseIssues,
        @DefaultValue("1000") @Min(1) @Max(10000) int maxReleaseCommits,
        @DefaultValue("5000") @Min(1) @Max(50000) int maxReferences,
        @DefaultValue("8388608") @Min(1) @Max(67108864) long maxSourceTextBytes,
        @DefaultValue("20") @Min(1) @Max(100) int maxBindings,
        @DefaultValue("500") @Min(1) @Max(5000) int maxMergeRequests,
        @DefaultValue("10000") @Min(1) @Max(50000) int maxObjects,
        @DefaultValue("15m") @NotNull Duration freshness,
        @DefaultValue("50") @Min(1) @Max(500) int retainedAttempts,
        @DefaultValue("500") @Min(1) @Max(5000) int maxAssignedIssues) {
    public TicketProperties {
        requireDuration(refreshTimeout, Duration.ofMinutes(10));
        requireDuration(requestTimeout, Duration.ofMinutes(2));
        requireDuration(freshness, Duration.ofDays(1));
        if (maxResponseBytes > maxTotalResponseBytes) {
            throw new IllegalArgumentException(io.givops.adapters.i18n.SdkMessages.format("sdk.ticketing.byte.budget.invalid"));
        }
    }

    private static void requireDuration(Duration value, Duration maximum) {
        if (value == null || value.isZero() || value.isNegative() || value.compareTo(maximum) > 0) {
            throw new IllegalArgumentException(io.givops.adapters.i18n.SdkMessages.format("sdk.ticketing.duration.invalid"));
        }
    }
}
