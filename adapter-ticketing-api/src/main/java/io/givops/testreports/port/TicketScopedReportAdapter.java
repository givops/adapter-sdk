package io.givops.testreports.port;

import io.givops.testreports.model.TestReportProviderModels.ReportInput;
import io.givops.ticketing.model.TicketModels.TicketProvider;
import io.givops.ticketing.model.TicketScopeModels.*;
import io.givops.ticketing.port.TicketProviderAdapter.Access;
import io.givops.ticketing.port.TicketRequestGate;

/** Ordinary-ticket acquisition for one module binding; report destination mappings remain independent. */
public interface TicketScopedReportAdapter {
    TicketProvider provider();
    ScopedOrdinaryDataset ordinaryVersionsScoped(ReportInput input, Scope scope, Access access, TicketRequestGate gate);
}
