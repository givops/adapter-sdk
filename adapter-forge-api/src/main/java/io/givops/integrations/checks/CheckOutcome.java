package io.givops.integrations.checks;

import java.time.Instant;
import java.util.List;

public record CheckOutcome(CheckStatus status, List<CheckOperation> operations, String errorCode, Instant retryAt) {
    public CheckOutcome { operations = List.copyOf(operations); }
}
