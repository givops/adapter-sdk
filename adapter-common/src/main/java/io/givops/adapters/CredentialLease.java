package io.givops.adapters;
import java.util.Map;
/** Host-owned credential capture; implementations must redact toString and never serialize this object. */
public interface CredentialLease {
    Map<String, String> fields();
    Map<String, String> destinations();
    String accessRevision();
    String mode();
    boolean legacyRuntime();
    /** Revalidate inside the current publication transaction when locked is true. */
    void requireCurrent(boolean locked) throws CredentialAccessException;
}
