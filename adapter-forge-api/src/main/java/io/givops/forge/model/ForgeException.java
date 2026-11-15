package io.givops.forge.model;

import org.springframework.http.HttpStatus;

public class ForgeException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public ForgeException(String code, HttpStatus status, String safeMessage) {
        super(safeMessage);
        this.code = code;
        this.status = status;
    }

    public String getCode() { return code; }
    public HttpStatus getStatus() { return status; }
}
