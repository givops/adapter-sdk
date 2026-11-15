package io.givops.adapters;
/** Private host credential access; expected failures cross this boundary only as CredentialAccessException. */
public interface CredentialHost {
    CredentialLease capture(CredentialRequest request) throws CredentialAccessException;
}
