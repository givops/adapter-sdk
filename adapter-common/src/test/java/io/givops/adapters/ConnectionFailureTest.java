package io.givops.adapters;

import static org.junit.jupiter.api.Assertions.*;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.nio.channels.UnresolvedAddressException;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertificateException;
import java.util.concurrent.CompletionException;
import javax.net.ssl.SSLHandshakeException;
import org.junit.jupiter.api.Test;

class ConnectionFailureTest {
    @Test
    void distinguishesTheUnderlyingNetworkAndHandshakeFailures() {
        assertEquals(ConnectionFailure.Category.DNS, ConnectionFailure.category(new CompletionException(new UnknownHostException())));
        assertEquals(ConnectionFailure.Category.DNS, ConnectionFailure.category(new ConnectException().initCause(new UnresolvedAddressException())));
        assertEquals(ConnectionFailure.Category.CONNECTION_REFUSED, ConnectionFailure.category(new ConnectException("Connection refused")));
        assertEquals(ConnectionFailure.Category.NETWORK_UNREACHABLE, ConnectionFailure.category(new NoRouteToHostException()));
        assertEquals(ConnectionFailure.Category.TIMEOUT, ConnectionFailure.category(new java.net.http.HttpConnectTimeoutException("timed out")));
        assertEquals(ConnectionFailure.Category.TLS_TRUST, ConnectionFailure.category(new SSLHandshakeException("failed").initCause(new CertPathBuilderException())));
        assertEquals(ConnectionFailure.Category.TLS_HOSTNAME, ConnectionFailure.category(new SSLHandshakeException("failed")
                .initCause(new CertificateException("No subject alternative DNS name matching secret-host found."))));
        assertEquals(ConnectionFailure.Category.TLS, ConnectionFailure.category(new SSLHandshakeException("protocol_version")));
        assertEquals(ConnectionFailure.Category.CANCELLED, ConnectionFailure.category(new InterruptedException()));
    }

    @Test
    void exposesOnlyRecognizedResponseMetadataAndFixedAdvice() {
        assertFalse(ConnectionFailure.evidence(new UnknownHostException("user:secret@example.test")).contains("secret"));
        assertEquals("HTTP 502 with an HTML page. Check the API or clone URL and any proxy sign-in page.",
                ConnectionFailure.httpEvidence(502, "text/html; secret=123"));
        assertEquals("HTTP 403 with a JSON response.", ConnectionFailure.httpEvidence(403, "application/problem+json"));
        assertEquals("HTTP 503 from the remote endpoint.", ConnectionFailure.httpEvidence(503, "secret-value"));
    }
}
