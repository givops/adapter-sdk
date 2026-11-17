package io.givops.forge.port;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import io.givops.catalog.ForgeProvider;
import io.givops.forge.model.ForgeConnectionSettings.Hosting;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ForgeContractsTest {
    @Test void hierarchyIsOptionalAndDoesNotCoupleDiscoveryOperations() {
        ForgeDiscoveryAdapter adapter = new ProjectOnlyDiscovery();
        assertThat(adapter.supports(ForgeProvider.GITLAB, Hosting.SAAS,
                ForgeDiscoveryAdapter.Operation.PROJECT_DISCOVERY)).isTrue();
        assertThat(adapter.supports(ForgeProvider.GITLAB, Hosting.SAAS,
                ForgeDiscoveryAdapter.Operation.GROUP_DISCOVERY)).isFalse();
        assertThat(adapter).isNotInstanceOf(ForgeGroupHierarchyAdapter.class);
    }

    /** An existing discovery implementation compiles without any hierarchy methods. */
    private static final class ProjectOnlyDiscovery implements ForgeDiscoveryAdapter {
        @Override public Support support(ForgeProvider provider, Hosting hosting) { return new Support(true, false); }
        @Override public io.givops.forge.discovery.DiscoveryModels.RemotePage<io.givops.forge.discovery.DiscoveryModels.RemoteGroup>
        groups(io.givops.forge.discovery.DiscoveryModels.Server server, ForgeAdapter.Access access, String query,
               int limit, int page, long lastId, ForgeRequestGate gate) { throw new UnsupportedOperationException(); }
        @Override public io.givops.forge.discovery.DiscoveryModels.RemotePage<io.givops.forge.discovery.DiscoveryModels.RemoteProject>
        projects(io.givops.forge.discovery.DiscoveryModels.Server server, ForgeAdapter.Access access, String groupId,
                 String query, int limit, int page, long lastId, ForgeRequestGate gate) { throw new UnsupportedOperationException(); }
        @Override public io.givops.forge.discovery.DiscoveryModels.RemoteProject
        project(io.givops.forge.discovery.DiscoveryModels.Server server, ForgeAdapter.Access access,
                String externalId, ForgeRequestGate gate) { throw new UnsupportedOperationException(); }
    }

    @Test void canonicalLifecyclePreservesUnknownFutureValues() {
        var mapper = tools.jackson.databind.json.JsonMapper.builder().build();
        assertThat(mapper.readValue("\"FUTURE\"", io.givops.forge.model.ChangeRequestLifecycle.class))
                .isEqualTo(io.givops.forge.model.ChangeRequestLifecycle.UNKNOWN);
        assertThat(mapper.readValue("\"OPEN\"", io.givops.forge.model.ChangeRequestLifecycle.class))
                .isEqualTo(io.givops.forge.model.ChangeRequestLifecycle.OPEN);
    }

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
