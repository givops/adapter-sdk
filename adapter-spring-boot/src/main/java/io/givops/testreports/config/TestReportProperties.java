package io.givops.testreports.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("givops.test-reports")
public record TestReportProperties(
        @DefaultValue("300s") Duration refreshTimeout,
        @DefaultValue("10s") Duration requestTimeout,
        @DefaultValue("500") @Min(1) @Max(5000) int maxHttpAttempts,
        @DefaultValue("100") @Min(1) @Max(1000) int maxPages,
        @DefaultValue("2097152") @Min(1) @Max(16777216) long maxResponseBytes,
        @DefaultValue("67108864") @Min(1) @Max(268435456) long maxTotalResponseBytes,
        @DefaultValue("5000") @Min(1) @Max(50000) int maxReportIssues,
        @DefaultValue("10000") @Min(1) @Max(100000) int maxOrdinaryIssues,
        @DefaultValue("2000") @Min(1) @Max(20000) int maxEventsPerIssue,
        @DefaultValue("100000") @Min(1) @Max(1000000) int maxEvents,
        @DefaultValue("15m") Duration freshness,
        @DefaultValue("50") @Min(1) @Max(500) int retainedAttempts) {
    public TestReportProperties {
        duration(refreshTimeout, Duration.ofMinutes(15));
        duration(requestTimeout, Duration.ofMinutes(2));
        duration(freshness, Duration.ofDays(1));
        if (maxResponseBytes > maxTotalResponseBytes) throw new IllegalArgumentException(io.givops.adapters.i18n.SdkMessages.format("sdk.reports.byte.budget.invalid"));
    }
    private static void duration(Duration value, Duration maximum) {
        if (value == null || value.isNegative() || value.isZero() || value.compareTo(maximum) > 0)
            throw new IllegalArgumentException(io.givops.adapters.i18n.SdkMessages.format("sdk.reports.duration.invalid"));
    }
}
