package io.givops.integrations.checks;

/** Operation names, error codes and evidence must be server-authored, never provider payloads. */
public record CheckOperation(String operation, OperationStatus status, String errorCode, String evidence) {}
