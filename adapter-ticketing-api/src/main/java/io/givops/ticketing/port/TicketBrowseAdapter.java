package io.givops.ticketing.port;

import io.givops.ticketing.model.TicketBrowserModels.*;
import io.givops.ticketing.model.TicketModels.TicketProvider;

/** Optional provider-neutral browser capability; every operation revalidates captured access and checks the gate. */
public interface TicketBrowseAdapter {
    TicketProvider provider();
    BrowseDescriptor describe(BrowserRequest request, TicketProviderAdapter.Access access, TicketRequestGate gate);
    TicketPage list(BrowserRequest request, BrowseQuery query, TicketProviderAdapter.Access access, TicketRequestGate gate);
    TicketDetail detail(BrowserRequest request, TicketRef ticket, TicketProviderAdapter.Access access, TicketRequestGate gate);
}
