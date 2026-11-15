package io.givops.adapters;
/** Provider-authored semantics for host freshness, retry and access-revocation policy. */
public enum FailureCategory { ACCESS_DENIED, NOT_FOUND, INVALID_RESPONSE, INVALID_REQUEST, TRANSIENT, UNSUPPORTED, UNKNOWN }
