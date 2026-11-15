package io.givops.forge.port;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import io.givops.catalog.ForgeProvider;
import io.givops.forge.model.ForgeConnectionSettings.Hosting;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ForgeContractsTest {
    @Test void openProviderIdentityHasValueEqualityAndRejectsInvalidNames() {
        assertThat(ForgeProvider.valueOf("CUSTOM_FORGE")).isEqualTo(ForgeProvider.valueOf("CUSTOM_FORGE"));
        assertThatThrownBy(() -> ForgeProvider.valueOf("bad-name")).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void ephemeralCredentialsAreCopiedAndRedacted() {
        var fields = new HashMap<>(Map.of("privateKey", "synthetic-secret"));
        var access = new ForgeAdapter.Access(fields, Hosting.SELF_MANAGED);
        fields.clear();
        assertThat(access.fields()).containsEntry("privateKey", "synthetic-secret");
        assertThat(access.toString()).doesNotContain("synthetic-secret");
        assertThatThrownBy(() -> access.fields().clear()).isInstanceOf(UnsupportedOperationException.class);
    }
}
