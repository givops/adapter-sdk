package io.givops.adapters.testkit;

import static org.assertj.core.api.Assertions.*;

import io.givops.adapters.CredentialAccessException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AdapterTestkitTest {
    @Test void signerIsDeterministicLengthPrefixedAndNamespaceBound() {
        var signer = AdapterTestkit.signer("synthetic-secret-not-for-production");
        String first = signer.sign("EXAMPLE", "v1", List.of("ab", "c"));
        assertThat(first).matches("[0-9a-f]{64}").isEqualTo(signer.sign("EXAMPLE", "v1", List.of("ab", "c")));
        assertThat(first).isNotEqualTo(signer.sign("EXAMPLE", "v1", List.of("a", "bc")))
                .isNotEqualTo(signer.sign("EXAMPLE", "v2", List.of("ab", "c")));
    }
    @Test void capturedLeaseNeverSilentlyBecomesFreshAgain() {
        var lease = AdapterTestkit.lease(Map.of("token", "synthetic-token"), Map.of("baseUrl", "https://example.test"), "one");
        lease.requireCurrent(false);
        assertThat(lease.lastLocked()).isFalse();
        lease.invalidate();
        assertThatThrownBy(() -> lease.requireCurrent(true)).isInstanceOf(CredentialAccessException.class);
        assertThat(lease.lastLocked()).isTrue();
        assertThat(lease.toString()).doesNotContain("synthetic-token");
        assertThatThrownBy(() -> lease.fields().put("token", "changed")).isInstanceOf(UnsupportedOperationException.class);
    }
}
