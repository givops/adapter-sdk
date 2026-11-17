package io.givops.ticketing.port;

import io.givops.ticketing.model.TicketBrowserModels.*;
import io.givops.ticketing.model.TicketModels.TicketProvider;

/** Optional provider-neutral browser capability; every operation revalidates captured access and checks the gate. */
public interface TicketContentAdapter {
    TicketProvider provider();
    TicketContent content(BrowserRequest request, TicketRef ticket, TicketProviderAdapter.Access access, TicketRequestGate gate);
}
