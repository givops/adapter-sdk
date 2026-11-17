package io.givops.ticketing.port;

import io.givops.ticketing.model.TicketBrowserModels.*;
import io.givops.ticketing.model.TicketModels.TicketProvider;
import io.givops.ticketing.model.TicketScopeModels.Scope;

/** Complete scoped browser support, including direct identity and transient content revalidation. */
public interface TicketScopedBrowseAdapter {
    TicketProvider provider();
    BrowseDescriptor describeScoped(BrowserRequest request, Scope scope, TicketProviderAdapter.Access access, TicketRequestGate gate);
    TicketPage listScoped(BrowserRequest request, Scope scope, BrowseQuery query, TicketProviderAdapter.Access access, TicketRequestGate gate);
    TicketDetail detailScoped(BrowserRequest request, Scope scope, TicketRef ticket, TicketProviderAdapter.Access access, TicketRequestGate gate);
    TicketContent contentScoped(BrowserRequest request, Scope scope, TicketRef ticket, TicketProviderAdapter.Access access, TicketRequestGate gate);
    Navigation navigateScoped(BrowserRequest request, Scope scope, TicketRef ticket, TicketProviderAdapter.Access access, TicketRequestGate gate);
}
