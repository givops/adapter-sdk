package io.givops.integrations.checks;

import io.givops.forge.port.ForgeRequestGate;

public interface CheckBudget extends ForgeRequestGate {
    int MAX_RESPONSE_BYTES = 256 * 1024;

    @Override
    default void acceptedObjects(int count) {}
}
