package io.givops.forge.port;

import java.time.Duration;

/** Each remote request, including discovery and fallbacks, needs admission immediately before I/O. */
public interface ForgeRequestGate extends io.givops.adapters.ConnectionTarget.Provider {
    Duration beforeRequest();
    void receivedBytes(long count);
    void acceptedObjects(int count);
    void check();
}
