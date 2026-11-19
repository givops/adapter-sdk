package io.givops.ticketing.port;

import java.time.Duration;

/** All outbound attempts, including retries, require admission immediately before I/O. */
public interface TicketRequestGate extends io.givops.adapters.ConnectionTarget.Provider {
    Duration beforeRequest();
    void receivedBytes(long count);
    void acceptedObjects(int count);
    void check();
    Duration remaining();
}
