package example.ticketing;

import static org.assertj.core.api.Assertions.*;

import io.givops.adapters.CapabilityInspection;
import io.givops.adapters.ContextSigner;
import io.givops.adapters.CredentialHost;
import io.givops.adapters.testkit.AdapterTestkit;
import io.givops.ticketing.model.TicketException;
import io.givops.ticketing.model.TicketModels.*;
import io.givops.ticketing.port.TicketProviderAdapter;
import io.givops.ticketing.port.TicketProviderDefinition;
import io.givops.ticketing.port.TicketRequestGate;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ExampleContractTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
    private final InstanceSource source = new InstanceSource(UUID.randomUUID(), "example", ExampleDefinition.PROVIDER,
            URI.create("https://example.test"), 3);
    private final TicketRequestGate gate = new TicketRequestGate() {
        public Duration beforeRequest() { return remaining(); }
        public void receivedBytes(long count) { }
        public void acceptedObjects(int count) { }
        public void check() { }
        public Duration remaining() { return Duration.ofSeconds(5); }
    };
    @Test void recognizesOnlyOwnedUrlSyntaxWithFixedSdkIssueGrammar() {
        var definition = new ExampleDefinition();
        assertThat(definition.issueReference(URI.create("https://example.test/tickets/ex-12")))
                .contains(new TicketProviderDefinition.IssueReference("EX-12", ""));
        assertThat(definition.issueReference(URI.create("https://example.test/browse/EX-12"))).isEmpty();
        assertThatThrownBy(() -> new TicketProviderDefinition.IssueReference("arbitrary", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }
    @Test void independentlyRegistersWithoutPrivateHostSources() {
        var lease = AdapterTestkit.lease(Map.of("apiToken", "synthetic-token"), Map.of(), "one");
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(ExampleAutoConfiguration.class))
                .withBean(CredentialHost.class, () -> request -> lease)
                .withBean(ContextSigner.class, () -> AdapterTestkit.signer("synthetic-key-for-example-contract-test"))
                .withBean(Clock.class, () -> clock).run(context -> {
                    assertThat(context).hasSingleBean(TicketProviderDefinition.class).hasSingleBean(TicketProviderAdapter.class);
                    AdapterTestkit.verifyDefinition(context.getBean(TicketProviderDefinition.class));
                });
    }
    @Test void productVersionsAndExplicitOutcomesDoNotChangeOrdinaryReads() {
        for (String product : new String[]{"17.10.2", "19.0", null}) {
            for (var state : CapabilityInspection.State.values()) {
                var lease = AdapterTestkit.lease(Map.of("apiToken", "synthetic-token"), Map.of(), "one");
                var adapter = new ExampleAdapter(request -> lease, AdapterTestkit.signer("synthetic-key-for-example-contract-test"), clock, product, state);
                var access = adapter.captureAccess(source);
                var input = new ProviderInput(source, List.of(new ProjectQuery(UUID.randomUUID(), "project-1", "EX", Set.of(), null)), clock.instant());
                assertThat(adapter.read(input, gate, access).projects()).hasSize(1);
                assertThat(adapter.inspections()).isZero();
                var inspection = adapter.inspect(source, "project-1", "ticketing.read", access, gate);
                assertThat(inspection.state()).isEqualTo(state);
                assertThat(inspection.productVersion()).isEqualTo(product);
                assertThat(inspection.apiProfile()).isEqualTo("example-rest-v1");
                assertThat(inspection.context()).isEqualTo(access.context());
                lease.invalidate();
                assertThatThrownBy(() -> adapter.current(source, access, true))
                        .isInstanceOfSatisfying(TicketException.class, failure -> assertThat(failure.code()).isEqualTo(TicketException.Code.SUPERSEDED));
                assertThat(lease.lastLocked()).isTrue();
            }
        }
    }
}
