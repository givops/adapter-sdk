package example.ticketing;

import io.givops.adapters.CapabilityInspection;
import io.givops.adapters.ContextSigner;
import io.givops.adapters.CredentialAccessException;
import io.givops.adapters.CredentialHost;
import io.givops.adapters.CredentialLease;
import io.givops.adapters.CredentialRequest;
import io.givops.ticketing.model.TicketException;
import io.givops.ticketing.model.TicketModels.*;
import io.givops.ticketing.port.TicketCapabilityInspector;
import io.givops.ticketing.port.TicketProviderAdapter;
import io.givops.ticketing.port.TicketRequestGate;
import java.time.Clock;
import java.util.List;
import io.givops.ticketing.model.TicketBrowserModels.*;
import io.givops.ticketing.port.*;
import java.util.Locale;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/** Metadata acquisition remains empty; independent browser capabilities expose a bounded synthetic space. */
public final class ExampleAdapter implements TicketProviderAdapter, TicketCapabilityInspector, TicketBrowseAdapter, TicketContentAdapter, TicketAssetAdapter, TicketNavigationAdapter {
    private final CredentialHost credentials;
    private final ContextSigner signer;
    private final Clock clock;
    private final String productVersion;
    private final CapabilityInspection.State inspectionState;
    private int inspections;
    private final ExamplePayload payload = ExamplePayload.packaged();
    public ExampleAdapter(CredentialHost credentials, ContextSigner signer, Clock clock, String productVersion,
                          CapabilityInspection.State inspectionState) {
        this.credentials = credentials; this.signer = signer; this.clock = clock;
        this.productVersion = productVersion; this.inspectionState = inspectionState;
    }
    @Override public TicketProvider provider() { return ExampleDefinition.PROVIDER; }
    private record Captured(CredentialLease lease, String context) implements Access {
        @Override public String toString() { return "Captured[redacted]"; }
    }
    @Override public String authenticationContext(InstanceSource source) { return captureAccess(source).context(); }
    @Override public Access captureAccess(InstanceSource source) {
        try {
            var lease = credentials.capture(new CredentialRequest(source.instanceId(), source.instanceKey(), provider().name(), source.baseUrl()));
            return new Captured(lease, signer.sign(provider().name(), "example-v1", List.of(source.instanceId().toString(),
                    source.baseUrl().toString(), lease.accessRevision(), lease.fields().get("apiToken"))));
        } catch (CredentialAccessException failure) {
            throw new TicketException(TicketException.Code.MISSING_CREDENTIALS, ExampleMessages.format("example.credentials"));
        }
    }
    @Override public boolean current(InstanceSource source, Access captured, boolean locked) {
        try { ((Captured) captured).lease().requireCurrent(locked); return true; }
        catch (CredentialAccessException failure) {
            throw new TicketException(TicketException.Code.SUPERSEDED, ExampleMessages.format("example.changed"));
        }
    }
    @Override public ProviderSnapshot read(ProviderInput input, TicketRequestGate gate) { return read(input, gate, captureAccess(input.source())); }
    @Override public ProviderSnapshot read(ProviderInput input, TicketRequestGate gate, Access captured) {
        current(input.source(), captured, false);
        gate.check();
        return new ProviderSnapshot(input.projects().stream().map(query -> new ProjectResult(query.ticketProjectId(),
                new ProjectObservation(query.externalProjectId(), query.projectKey(), payload.optionalName(query.projectKey()), input.source().baseUrl(), clock.instant()),
                List.of(), List.of(), query.issueKeys(), new ReleaseMembership(ReleaseMappingState.NOT_REQUESTED, null, List.of(), clock.instant()))).toList());
    }
    @Override public CapabilityInspection inspect(InstanceSource source, String externalProjectId, String capability,
                                                Access access, TicketRequestGate gate) {
        current(source, access, false); gate.check(); inspections++;
        var state = capability.equals("ticketing.read") ? inspectionState : CapabilityInspection.State.UNSUPPORTED;
        return new CapabilityInspection(provider().name(), capability, state, payload.optionalName(ExampleMessages.format("example.synthetic")),
                productVersion, "example-rest-v1", access.context(), clock.instant());
    }
    public int inspections() { return inspections; }

    private static final SourceRevision SYNTHETIC_REVISION = new SourceRevision("synthetic", "fixture-v1", true);
    private static final byte[] ASSET = "A bounded synthetic attachment.\n".getBytes(StandardCharsets.UTF_8);
    private static final AssetRef ATTACHMENT = new AssetRef("attachment-1", "text/plain", ASSET.length,
            "sha256:" + digest(ASSET), "fixture.txt");
    private static String digest(byte[] bytes) {
        try { return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes)); }
        catch (java.security.NoSuchAlgorithmException failure) { throw new IllegalStateException(failure); }
    }
    private void browseCheck(BrowserRequest request, Access access, TicketRequestGate gate) {
        current(request.source(), access, false); gate.check();
        if (request.revision() != null && !SYNTHETIC_REVISION.equals(request.revision()))
            throw new TicketException(TicketException.Code.SUPERSEDED, "Synthetic revision is unavailable.");
    }
    private TicketSummary summary(BrowserRequest request, int id) {
        var outcome = id == 3 ? Outcome.CANCELLED : id == 2 ? Outcome.SUCCEEDED : Outcome.OPEN;
        return new TicketSummary(new TicketRef(request.source().instanceId(), request.externalProjectId(), "fixture-" + id),
                request.projectKey() + "-" + id, List.of("First synthetic ticket", "Completed synthetic ticket", "Cancelled synthetic ticket").get(id - 1),
                outcome.name(), outcome == Outcome.OPEN ? "TODO" : "DONE", Lifecycle.ACTIVE, outcome, List.of("fixture"),
                request.source().baseUrl().resolve("/tickets/" + request.projectKey() + "-" + id), clock.instant(), true, SYNTHETIC_REVISION);
    }
    private TicketSummary owned(BrowserRequest request, TicketRef ticket) {
        if (!request.source().instanceId().equals(ticket.instanceId()) || !request.externalProjectId().equals(ticket.externalProjectId()))
            throw new TicketException(TicketException.Code.INVALID_CONFIGURATION, "Ticket belongs to another source.");
        for (int id = 1; id <= 3; id++) if (ticket.externalIssueId().equals("fixture-" + id)) return summary(request, id);
        throw new TicketException(TicketException.Code.NOT_FOUND, "Synthetic ticket is unavailable.");
    }
    @Override public BrowseDescriptor describe(BrowserRequest request, Access access, TicketRequestGate gate) {
        browseCheck(request, access, gate);
        return new BrowseDescriptor(List.of("text:key-title-literal-nfc", "statuses", "categories", "labels:any"),
                List.of(Lifecycle.ACTIVE), List.of("key-asc"), true, true, true);
    }
    @Override public TicketPage list(BrowserRequest request, BrowseQuery query, Access access, TicketRequestGate gate) {
        browseCheck(request, access, gate);
        if (query.lifecycle() != Lifecycle.ACTIVE || !query.sort().equals("key-asc"))
            throw new TicketException(TicketException.Code.INVALID_CONFIGURATION, "Unsupported synthetic query.");
        String text = query.text().toLowerCase(Locale.ROOT);
        var all = java.util.stream.IntStream.rangeClosed(1, 3).mapToObj(id -> summary(request, id))
                .filter(ticket -> ticket.displayKey().toLowerCase(Locale.ROOT).contains(text) || ticket.title().toLowerCase(Locale.ROOT).contains(text))
                .filter(ticket -> query.statuses().isEmpty() || query.statuses().contains(ticket.nativeStatus()))
                .filter(ticket -> query.categories().isEmpty() || query.categories().contains(ticket.category()))
                .filter(ticket -> query.labels().isEmpty() || query.labels().stream().anyMatch(ticket.labels()::contains)).toList();
        int offset;
        try { offset = query.continuation() == null ? 0 : Integer.parseInt(query.continuation()); }
        catch (NumberFormatException invalid) { throw new TicketException(TicketException.Code.INVALID_CONFIGURATION, "Invalid synthetic continuation."); }
        if (offset < 0 || offset > all.size()) throw new TicketException(TicketException.Code.INVALID_CONFIGURATION, "Invalid synthetic continuation.");
        int end = Math.min(all.size(), offset + query.pageSize());
        gate.acceptedObjects(end - offset);
        return new TicketPage(all.subList(offset, end), end < all.size() ? Integer.toString(end) : null,
                (long) all.size(), SYNTHETIC_REVISION, request.capturedAt(), query);
    }
    @Override public TicketDetail detail(BrowserRequest request, TicketRef ticket, Access access, TicketRequestGate gate) {
        browseCheck(request, access, gate);
        return new TicketDetail(owned(request, ticket), clock.instant(), null, List.of(), navigate(request, ticket, access, gate));
    }
    @Override public TicketContent content(BrowserRequest request, TicketRef ticket, Access access, TicketRequestGate gate) {
        browseCheck(request, access, gate); var item = owned(request, ticket);
        String text = "# " + item.title() + "\n\nFull synthetic narrative, fetched only on demand.\n\n[Attachment](fixture.txt)\n";
        gate.receivedBytes(text.getBytes(StandardCharsets.UTF_8).length);
        return new TicketContent(ticket, SYNTHETIC_REVISION, ContentFormat.MARKDOWN, text, List.of(ATTACHMENT),
                List.of(new LinkRef("fixture.txt", null, null, ATTACHMENT.id())));
    }
    @Override public TicketAsset asset(BrowserRequest request, TicketRef ticket, String assetId, Access access, TicketRequestGate gate) {
        browseCheck(request, access, gate); owned(request, ticket);
        if (request.revision() == null || !ATTACHMENT.id().equals(assetId))
            throw new TicketException(TicketException.Code.INVALID_CONFIGURATION, "Asset requires its manifest revision and identity.");
        var stream = new java.io.FilterInputStream(new ByteArrayInputStream(ASSET)) {
            @Override public int read() throws java.io.IOException { browseCheck(request, access, gate); int value = super.read(); if (value >= 0) gate.receivedBytes(1); return value; }
            @Override public int read(byte[] buffer, int offset, int count) throws java.io.IOException {
                browseCheck(request, access, gate); int read = super.read(buffer, offset, count); if (read > 0) gate.receivedBytes(read); return read;
            }
        };
        return new TicketAsset() {
            public TicketRef ref() { return ticket; }
            public SourceRevision revision() { return SYNTHETIC_REVISION; }
            public AssetRef metadata() { return ATTACHMENT; }
            public java.io.InputStream stream() { return stream; }
            public void close() throws java.io.IOException { stream.close(); }
        };
    }
    @Override public Navigation navigate(BrowserRequest request, TicketRef ticket, Access access, TicketRequestGate gate) {
        browseCheck(request, access, gate);
        return new Navigation(request.source().baseUrl(), ticket == null ? null : owned(request, ticket).webUrl());
    }
}
