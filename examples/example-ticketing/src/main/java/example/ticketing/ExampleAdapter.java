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

/** Returns synthetic empty projects; no HTTP calls or product-specific feature claims. */
public final class ExampleAdapter implements TicketProviderAdapter, TicketCapabilityInspector {
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
}
