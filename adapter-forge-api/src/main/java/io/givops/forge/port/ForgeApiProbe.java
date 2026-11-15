package io.givops.forge.port;

import io.givops.catalog.ForgeProvider;
import io.givops.integrations.checks.CheckBudget;
import io.givops.integrations.checks.CheckOutcome;

public interface ForgeApiProbe {
    ForgeProvider provider();
    void validate(ForgeCheckInput input);
    CheckOutcome probe(ForgeCheckInput input, CheckBudget budget);
}
