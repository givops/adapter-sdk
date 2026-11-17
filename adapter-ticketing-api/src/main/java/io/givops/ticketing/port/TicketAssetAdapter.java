package io.givops.ticketing.port;

import io.givops.ticketing.model.TicketBrowserModels.*;
import io.givops.ticketing.model.TicketModels.TicketProvider;

/** Optional provider-neutral browser capability; every operation revalidates captured access and checks the gate. */
public interface TicketAssetAdapter {
    TicketProvider provider();
    TicketAsset asset(BrowserRequest request, TicketRef ticket, String assetId, TicketProviderAdapter.Access access, TicketRequestGate gate);
}
