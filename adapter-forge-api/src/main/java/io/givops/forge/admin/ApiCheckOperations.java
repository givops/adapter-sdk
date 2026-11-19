package io.givops.forge.admin;

import io.givops.adapters.ConnectionFailure;
import io.givops.forge.model.ForgeException;
import io.givops.integrations.checks.CheckOperation;
import io.givops.integrations.checks.CheckOutcome;
import io.givops.integrations.checks.CheckStatus;
import io.givops.integrations.checks.ConnectionCheckException;
import io.givops.integrations.checks.OperationStatus;
import java.net.http.HttpHeaders;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.function.Supplier;

/** Accumulates bounded, endpoint-specific evidence without persisting provider payloads. */
public final class ApiCheckOperations {
    private final ArrayList<CheckOperation> operations = new ArrayList<>();
    private final Instant now;
    private String firstError;
    private Instant retryAt;
    private boolean stopped;

    public ApiCheckOperations(Instant now) { this.now = now; }

    public void observe(String name, String evidence, Supplier<Observation> request) {
        if (stopped) {
            operations.add(new CheckOperation(name, OperationStatus.NOT_RUN, null, null));
            return;
        }
        try {
            var observation = request.get();
            if (observation == null) {
                operations.add(new CheckOperation(name, OperationStatus.SUCCEEDED, null, evidence));
            } else {
                String diagnostic = observation.httpStatus() == null ? null : ConnectionFailure.httpEvidence(
                        observation.httpStatus(), observation.headers().firstValue("Content-Type").orElse(null));
                fail(name, observation.status(), observation.errorCode(), diagnostic);
                if (observation.errorCode().endsWith("_rate_limited")) retryAt = retry(observation.headers());
            }
        } catch (ConnectionCheckException exception) {
            if ("connection_check_superseded".equals(exception.getCode())) throw exception;
            fail(name, OperationStatus.FAILED, exception.getCode(), null);
        } catch (ForgeException exception) {
            fail(name, OperationStatus.FAILED, exception.getCode(), exception.diagnosticEvidence());
        }
    }

    private void fail(String name, OperationStatus status, String error, String diagnostic) {
        if (firstError == null) firstError = error;
        operations.add(new CheckOperation(name, status, error, diagnostic));
        // Permission and availability may differ across the saved endpoints.
        stopped = !error.endsWith("_forbidden") && !error.endsWith("_not_found");
    }

    public CheckOutcome outcome() {
        return new CheckOutcome(firstError == null ? CheckStatus.SUCCEEDED : CheckStatus.FAILED,
                operations, firstError, retryAt);
    }

    private Instant retry(HttpHeaders headers) {
        String after = headers.firstValue("Retry-After").orElse(null);
        if (after != null) {
            try {
                long seconds = Long.parseLong(after);
                if (seconds >= 0 && seconds <= 2_592_000) return now.plusSeconds(seconds);
            } catch (NumberFormatException | DateTimeException | ArithmeticException ignored) {
                try {
                    var parsed = ZonedDateTime.parse(after, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
                    if (!parsed.isBefore(now) && !parsed.isAfter(now.plusSeconds(2_592_000))) return parsed;
                } catch (DateTimeException ignoredDate) {
                    // A malformed advisory header cannot change the failure classification.
                }
            }
        }
        String reset = headers.firstValue("X-RateLimit-Reset").or(() -> headers.firstValue("RateLimit-Reset")).orElse(null);
        if (reset != null) {
            try {
                var parsed = Instant.ofEpochSecond(Long.parseLong(reset));
                if (!parsed.isBefore(now) && !parsed.isAfter(now.plusSeconds(2_592_000))) return parsed;
            } catch (NumberFormatException | DateTimeException ignored) {
                // Use the default guidance below.
            }
        }
        return now.plusSeconds(60);
    }

    public record Observation(OperationStatus status, String errorCode, HttpHeaders headers, Integer httpStatus) {
        public Observation(OperationStatus status, String errorCode, HttpHeaders headers) {
            this(status, errorCode, headers, null);
        }
    }
}
