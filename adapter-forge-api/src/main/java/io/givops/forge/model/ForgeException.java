package io.givops.forge.model;

import io.givops.adapters.ConnectionFailure;
import org.springframework.http.HttpStatus;

public class ForgeException extends RuntimeException {
    private final String code;
    private final HttpStatus status;
    private final io.givops.adapters.FailureCategory category;
    private final String diagnosticEvidence;

    public ForgeException(String code, HttpStatus status, String safeMessage) {
        this(code, status, safeMessage, switch (status) {
            case UNAUTHORIZED, FORBIDDEN -> io.givops.adapters.FailureCategory.ACCESS_DENIED;
            case NOT_FOUND -> io.givops.adapters.FailureCategory.NOT_FOUND;
            case TOO_MANY_REQUESTS, SERVICE_UNAVAILABLE, GATEWAY_TIMEOUT -> io.givops.adapters.FailureCategory.TRANSIENT;
            case BAD_REQUEST -> io.givops.adapters.FailureCategory.INVALID_REQUEST;
            case UNPROCESSABLE_CONTENT -> io.givops.adapters.FailureCategory.UNSUPPORTED;
            default -> io.givops.adapters.FailureCategory.UNKNOWN;
        });
    }
    public ForgeException(String code, HttpStatus status, String safeMessage, io.givops.adapters.FailureCategory category) {
        this(code, status, safeMessage, category, null);
    }

    private ForgeException(String code, HttpStatus status, String safeMessage, io.givops.adapters.FailureCategory category,
                           String diagnosticEvidence) {
        super(diagnosticEvidence == null ? safeMessage : safeMessage + " " + diagnosticEvidence);
        this.diagnosticEvidence = diagnosticEvidence;
        this.category = java.util.Objects.requireNonNull(category);
        this.code = code;
        this.status = status;
    }

    public io.givops.adapters.FailureCategory category() { return category; }
    public String getCode() { return code; }
    public HttpStatus getStatus() { return status; }

    public String diagnosticEvidence() { return diagnosticEvidence; }

    public ForgeException withTransportFailure(Throwable failure) {
        return new ForgeException(code, status, getMessage(), category, ConnectionFailure.evidence(failure));
    }

    public ForgeException withHttpResponse(int remoteStatus, String contentType) {
        return new ForgeException(code, status, getMessage(), category, ConnectionFailure.httpEvidence(remoteStatus, contentType));
    }
}
