package io.givops.adapters;

import io.givops.adapters.i18n.SdkMessages;

/** Expected host credential capture or freshness failure. Never carries a secret or private host cause.
 * Adapters map capture failures to MISSING_CREDENTIALS and revalidation failures to SUPERSEDED.
 */
public final class CredentialAccessException extends RuntimeException {
    public CredentialAccessException() { super(SdkMessages.format("sdk.credential.unavailable"), null); }
}
