package io.givops.forge.model;

import org.springframework.http.HttpStatus;

public class ForgeException extends RuntimeException {
    private final String code;
    private final HttpStatus status;
    private final io.givops.adapters.FailureCategory category;

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
        super(safeMessage);
        this.category = java.util.Objects.requireNonNull(category);
        this.code = code;
        this.status = status;
    }

    public io.givops.adapters.FailureCategory category() { return category; }
    public String getCode() { return code; }
    public HttpStatus getStatus() { return status; }
}
