package example.ticketing;

import static org.assertj.core.api.Assertions.*;
import io.givops.adapters.CapabilityInspection;
import io.givops.adapters.testkit.AdapterTestkit;
import io.givops.ticketing.model.TicketBrowserModels.*;
import io.givops.ticketing.model.TicketModels.*;
import io.givops.ticketing.model.TicketException;
import io.givops.ticketing.port.TicketRequestGate;
import java.net.URI;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class ExampleBrowserTest {
    final Instant now = Instant.parse("2026-01-01T00:00:00Z");
    final InstanceSource source = new InstanceSource(UUID.randomUUID(), "synthetic", ExampleDefinition.PROVIDER, URI.create("https://example.test"), 1);
    final BrowserRequest request = new BrowserRequest(source, UUID.randomUUID(), "project-1", "EX", null, now);
    final AdapterTestkit.MutableCredentialLease lease = AdapterTestkit.lease(Map.of("apiToken", "synthetic"), Map.of(), "one");
    final ExampleAdapter adapter = new ExampleAdapter(target -> lease, AdapterTestkit.signer("synthetic-key"), Clock.fixed(now, ZoneOffset.UTC), "1", CapabilityInspection.State.AVAILABLE);
    final TicketRequestGate gate = new TicketRequestGate() {
        public Duration beforeRequest() { return remaining(); }
        public Duration remaining() { return Duration.ofSeconds(5); }
        public void check() { }
        public void receivedBytes(long count) { }
        public void acceptedObjects(int count) { }
    };
    BrowseQuery query(String text, int size, String continuation) { return new BrowseQuery(text, null, null, null, null, null, size, continuation); }
    @Test void listsUnobservedTicketsWithCompleteStablePagesAndDistinctCancellation() {
        var access = adapter.captureAccess(source);
        assertThat(adapter.read(new ProviderInput(source, List.of(new ProjectQuery(request.ticketProjectId(), "project-1", "EX", Set.of(), null)), now), gate).projects().getFirst().issues()).isEmpty();
        var one = adapter.list(request, query("", 2, null), access, gate);
        var two = adapter.list(request, query("", 2, one.nextContinuation()), access, gate);
        assertThat(one.exactTotal()).isEqualTo(3); assertThat(two.nextContinuation()).isNull();
        assertThat(one.items()).extracting(TicketSummary::displayKey).containsExactly("EX-1", "EX-2");
        assertThat(two.items()).singleElement().extracting(TicketSummary::outcome).isEqualTo(Outcome.CANCELLED);
        assertThat(adapter.list(request, query(" COMPLETED ", 50, null), access, gate).items()).singleElement()
                .extracting(TicketSummary::outcome).isEqualTo(Outcome.SUCCEEDED);
        var filtered = new BrowseQuery("", Lifecycle.ACTIVE, List.of("OPEN"), List.of("TODO"), List.of("fixture"), "key-asc", 50, null);
        assertThat(adapter.list(request, filtered, access, gate).items()).hasSize(1);
        assertThatThrownBy(() -> adapter.list(request, new BrowseQuery("", Lifecycle.DRAFT, null, null, null, null, 50, null), access, gate)).isInstanceOf(TicketException.class);
    }
    @Test void servesRevisionBoundContentAndAssetsAndStopsRevokedReads() throws Exception {
        var access = adapter.captureAccess(source);
        var item = adapter.list(request, query("EX-1", 50, null), access, gate).items().getFirst();
        var pinned = new BrowserRequest(source, request.ticketProjectId(), "project-1", "EX", item.revision(), now);
        var body = adapter.content(pinned, item.ref(), access, gate);
        AdapterTestkit.verifyBrowserContent(body, item);
        assertThat(body.text()).contains("Full synthetic narrative"); assertThat(body.toString()).doesNotContain(body.text());
        try (var asset = adapter.asset(pinned, item.ref(), body.assets().getFirst().id(), access, gate)) {
            assertThat(asset.revision()).isEqualTo(body.revision()); assertThat(asset.stream().readAllBytes()).hasSize((int) asset.metadata().size());
        }
        assertThatThrownBy(() -> adapter.asset(request, item.ref(), "attachment-1", access, gate)).isInstanceOf(TicketException.class);
        var wrong = new BrowserRequest(source, request.ticketProjectId(), "project-1", "EX", new SourceRevision("synthetic", "missing", true), now);
        assertThatThrownBy(() -> adapter.content(wrong, item.ref(), access, gate)).isInstanceOf(TicketException.class);
        var stream = adapter.asset(pinned, item.ref(), "attachment-1", access, gate);
        lease.invalidate();
        try (stream) { assertThatThrownBy(() -> stream.stream().read()).isInstanceOf(TicketException.class); }
    }
    @Test void refusesForeignTicketAndMalformedQueries() {
        var access = adapter.captureAccess(source);
        assertThatThrownBy(() -> adapter.detail(request, new TicketRef(UUID.randomUUID(), "project-1", "fixture-1"), access, gate)).isInstanceOf(TicketException.class);
        assertThatThrownBy(() -> query("x".repeat(257), 50, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> query("", 101, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> adapter.list(request, query("", 50, "bad"), access, gate)).isInstanceOf(TicketException.class);
    }
}
