package io.givops.adapters;

import io.givops.adapters.i18n.SdkMessages;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;
import java.nio.channels.UnresolvedAddressException;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;

/** Classifies transport causes without returning exception text, remote payloads or headers. */
public final class ConnectionFailure {
    public enum Category {
        DNS, CONNECTION_REFUSED, NETWORK_UNREACHABLE, TIMEOUT, TLS_TRUST, TLS_HOSTNAME, TLS, CANCELLED, CONNECTION
    }

    private ConnectionFailure() {}

    public static Category category(Throwable failure) {
        Category fallback = Category.CONNECTION;
        // HTTP clients wrap the socket failure. Prefer a specific inner cause over its outer wrapper.
        for (int depth = 0; failure != null && depth < 24; depth++, failure = failure.getCause()) {
            if (failure instanceof UnknownHostException || failure instanceof UnresolvedAddressException) return Category.DNS;
            if (failure instanceof NoRouteToHostException) return Category.NETWORK_UNREACHABLE;
            if (failure instanceof HttpTimeoutException || failure instanceof SocketTimeoutException
                    || failure instanceof TimeoutException) return Category.TIMEOUT;
            if (failure instanceof InterruptedException || failure instanceof CancellationException) return Category.CANCELLED;
            if (failure instanceof CertificateException || failure instanceof SSLException) {
                String reason = String.valueOf(failure.getMessage()).toLowerCase(Locale.ROOT);
                if (reason.contains("subject alternative") || reason.contains("no name matching")
                        || reason.contains("hostname verification")) return Category.TLS_HOSTNAME;
                if (failure instanceof CertificateException || failure instanceof SSLPeerUnverifiedException
                        || reason.contains("pkix") || reason.contains("certification path")) fallback = Category.TLS_TRUST;
                else if (fallback != Category.TLS_TRUST) fallback = Category.TLS;
            } else if (failure instanceof CertPathBuilderException || failure instanceof CertPathValidatorException) {
                fallback = Category.TLS_TRUST;
            } else if (failure instanceof java.net.SocketException) {
                String reason = String.valueOf(failure.getMessage()).toLowerCase(Locale.ROOT);
                if (reason.contains("unreachable") || reason.contains("no route")) return Category.NETWORK_UNREACHABLE;
                if (failure instanceof ConnectException || reason.contains("connection refused")) fallback = Category.CONNECTION_REFUSED;
            }
        }
        return fallback;
    }

    public static String evidence(Throwable failure) {
        return SdkMessages.format("sdk.connection." + category(failure).name().toLowerCase(Locale.ROOT));
    }

    /** HTTP diagnostics use only the status and a recognized media type, never provider text. */
    public static String httpEvidence(int status, String contentType) {
        if (status < 100 || status > 599) return SdkMessages.format("sdk.connection.invalid_status");
        String type = contentType == null ? "" : contentType.split(";", 2)[0].strip().toLowerCase(Locale.ROOT);
        String key;
        if (type.equals("text/html") || type.equals("application/xhtml+xml")) key = "html";
        else if (type.equals("application/json") || type.startsWith("application/") && type.endsWith("+json")) key = "json";
        else key = "http";
        return SdkMessages.format("sdk.connection." + key, Map.of("status", Integer.toString(status)));
    }
}
