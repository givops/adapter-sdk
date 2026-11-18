package io.givops.ticketing.model;

import io.givops.adapters.CapabilityInspection.State;
import io.givops.ticketing.model.TicketScopeModels.*;
import io.givops.ticketing.model.TicketModels.VersionObservation;
import io.givops.testreports.model.TestReportProviderModels.OrdinaryVersion;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ScopeContractsTest {
    private final Field field = new Field("partition", "Team", ValueKind.OPTION, Cardinality.ONE, Match.EQUALS, State.AVAILABLE, null);
    @Test void prefixIsLiteralAndPreservesNativeIdentityAndOldConstructors() {
        var scope = new Scope(UUID.randomUUID(), field, "123", "Back .* ", 1, 2);
        assertThat(scope.canonicalName("Back .* 1.0.0")).isEqualTo("1.0.0");
        assertThat(scope.canonicalName("Back .* ")).isNull();
        assertThat(scope.canonicalName("Back abc 1.0.0")).isNull();
        assertThat(scope.canonicalName("back .* 1.0.0")).isNull();
        assertThat(scope.providerName("1.0.0")).isEqualTo("Back .* 1.0.0");
        var old = new VersionObservation("id", "native", false, false, null, Instant.EPOCH);
        assertThat(old.canonicalName()).isEqualTo("native");
        var oldOrdinary = new OrdinaryVersion(UUID.randomUUID(), "id", "native", false, false, null);
        assertThat(oldOrdinary.canonicalName()).isEqualTo("native");
    }
    @Test void commandReceiptKeepsCapturedLocalAndExactProviderNamesAndPreservesLegacyConstruction() {
        var old = new io.givops.release.commands.ReleaseCommandModels.Command(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                io.givops.release.commands.ReleaseCommandModels.Action.VERSION_ONLY, "SUCCEEDED", Instant.EPOCH, Instant.EPOCH,
                "provider-id", null, null, null, null, false);
        assertThat(old.versionName()).isNull();
        assertThat(old.providerVersionName()).isNull();
        var captured = new io.givops.release.commands.ReleaseCommandModels.Command(old.id(), old.projectId(), old.definitionId(),
                old.action(), old.state(), old.createdAt(), old.completedAt(), old.versionId(), null, null, null, null, false,
                "1.0.0", " Back  1.0.0 ");
        assertThat(captured.versionName()).isEqualTo("1.0.0");
        assertThat(captured.providerVersionName()).isEqualTo(" Back  1.0.0 ");
    }
    @Test void malformedOrUnavailableScopeCannotBecomeActive() {
        for (var prefix : new String[]{null, "", "   ", "Back\n"})
            assertThatThrownBy(() -> new Scope(UUID.randomUUID(), field, "123", prefix, 0, 0)).isInstanceOf(IllegalArgumentException.class);
        var unavailable = new Field("partition", "Team", ValueKind.OPTION, Cardinality.ONE, Match.EQUALS, State.UNKNOWN, null);
        assertThatThrownBy(() -> new Scope(UUID.randomUUID(), unavailable, "123", "Back ", 0, 0)).isInstanceOf(IllegalArgumentException.class);
        var ignored = new Field("partition", "Team", ValueKind.OPTION, Cardinality.MANY, Match.EQUALS, State.AVAILABLE, null);
        assertThatThrownBy(() -> new Scope(UUID.randomUUID(), ignored, "123", "Back ", 0, 0)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void legacyProvidersDoNotAccidentallyImplementScopedPorts() {
        assertThat(io.givops.ticketing.port.TicketScopedProviderAdapter.class.isAssignableFrom(io.givops.ticketing.port.TicketProviderAdapter.class)).isFalse();
        assertThat(io.givops.ticketing.port.TicketScopedBrowseAdapter.class.isAssignableFrom(io.givops.ticketing.port.TicketBrowseAdapter.class)).isFalse();
        assertThat(io.givops.testreports.port.TicketScopedReportAdapter.class.isAssignableFrom(io.givops.testreports.port.TestReportProviderAdapter.class)).isFalse();
    }
}
