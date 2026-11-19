package io.givops.adapters;

import java.net.URI;
import java.net.http.HttpClient;

/** A host may select TLS for a registered instance and destination. The host owns any selected client. */
@FunctionalInterface
public interface ConnectionClients {
    ConnectionClients SYSTEM = (target, destination, systemClient) -> systemClient;

    HttpClient select(ConnectionTarget target, URI destination, HttpClient systemClient);
}
