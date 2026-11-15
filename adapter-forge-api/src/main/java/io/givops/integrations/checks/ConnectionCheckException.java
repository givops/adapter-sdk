package io.givops.integrations.checks;

import org.springframework.http.HttpStatus;

public final class ConnectionCheckException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public ConnectionCheckException(String code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() { return code; }
    public HttpStatus getStatus() { return status; }
}
