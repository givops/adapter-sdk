package io.givops.ticketing.model;

import io.givops.adapters.ConnectionFailure;
import java.time.Instant;
import java.util.UUID;

public final class TicketException extends RuntimeException {
    public enum Code {
        NOT_FOUND, INVALID_CONFIGURATION, REVISION_CONFLICT, DISABLED, NOT_OBSERVED, STALE_CONTEXT,
        IN_PROGRESS, SUPERSEDED, UNSUPPORTED_PROVIDER, MISSING_CREDENTIALS, AUTHENTICATION_FAILED,
        AUTHORIZATION_FAILED, PROVIDER_ERROR, INVALID_RESPONSE, RATE_LIMITED, TIMEOUT, LIMIT_EXCEEDED, INTERNAL_ERROR
    }

    private final Code code;
    private final Instant retryAt;
    private final UUID refreshId;
    private final String diagnosticEvidence;

    public TicketException(Code code, String safeMessage) {
        this(code, safeMessage, null);
    }

    public TicketException(Code code, String safeMessage, Instant retryAt) {
        this(code, safeMessage, retryAt, null, null);
    }

    private TicketException(Code code, String safeMessage, Instant retryAt, UUID refreshId, String diagnosticEvidence) {
        super(safeMessage);
        this.diagnosticEvidence = diagnosticEvidence;
        this.code = code;
        this.retryAt = retryAt;
        this.refreshId = refreshId;
    }

    public Code code() { return code; }

    public Instant retryAt() { return retryAt; }

    public UUID refreshId() { return refreshId; }

    public TicketException forRefresh(UUID id) {
        return new TicketException(code, getMessage(), retryAt, id, diagnosticEvidence);
    }

    public String diagnosticEvidence() { return diagnosticEvidence; }

    public TicketException withTransportFailure(Throwable failure) { return withEvidence(ConnectionFailure.evidence(failure)); }

    public TicketException withHttpResponse(int status, String contentType) { return withEvidence(ConnectionFailure.httpEvidence(status, contentType)); }

    private TicketException withEvidence(String evidence) {
        return new TicketException(code, getMessage() + " " + evidence, retryAt, refreshId, evidence);
    }
}
